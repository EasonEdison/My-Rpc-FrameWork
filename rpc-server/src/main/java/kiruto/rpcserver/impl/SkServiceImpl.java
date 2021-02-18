package kiruto.rpcserver.impl;

import doraemon.entity.Result;
import doraemon.service.SkService;
import kiruto.annotation.RpcService;
import kiruto.rpcserver.entity.SqlVo;
import kiruto.rpcserver.entity.SuccessKilled;
import kiruto.rpcserver.enums.SeckillStateEnum;
import kiruto.rpcserver.exception.SkException;
import kiruto.rpcserver.mapper.SeckillComdMapper;
import kiruto.rpcserver.mapper.SuccesskilledMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RpcService(group = "JoJo", version = "1")
public class SkServiceImpl implements SkService {

    /**
     * bean的装配是单例的，所以多个线程公用的一个lock.
     */
    private Lock lock = new ReentrantLock(true);

    @Autowired
    private SuccesskilledMapper successkilledMapper;

    @Autowired
    private SeckillComdMapper seckillComdMapper;


    @Override
    public Long getSuccessCount(long skId) {
        String sql = "select count(*) from success_killed where seckill_id = %s";
        Long num = ((Number) successkilledMapper.selectSql(new SqlVo(String.format(sql, skId)))).longValue();
        return num;
    }

    @Override
    public void deleteSuccess(long skId) {
        String sql = "delete from success_killed where seckill_id = %s";
        successkilledMapper.deleteSql(new SqlVo(String.format(sql, skId)));
        // 数量更新
        sql = "update seckill set number = 100 where seckill_id = %s";
        seckillComdMapper.updateSql(new SqlVo(String.format(sql, skId)));
    }

    @Override
    public Result SkLock(long skId, long userId) {
        lock.lock();
        try {
            String sql = "select number from seckill where seckill_id = %s";
            Object object = seckillComdMapper.selectSql(new SqlVo(String.format(sql, skId)));
            Long number = ((Number) object).longValue();
            if (number > 0) {
                // 更新数量
                sql = "update seckill set number = number - 1 where seckill_id = %s";
                seckillComdMapper.updateSql(new SqlVo(String.format(sql, skId)));
                // 生成订单
                SuccessKilled successKilled = new SuccessKilled();
                successKilled.setSeckillId(skId);
                successKilled.setUserId(userId);
                successKilled.setState(Short.parseShort(number + ""));
                successKilled.setCreateTime(new Timestamp(System.currentTimeMillis()));
                successkilledMapper.insert(successKilled);
            } else {
                return Result.error(SeckillStateEnum.END);
            }
        } catch (Exception e) {
            throw new SkException("秒杀异常", e);
        } finally {
            // 最后释放锁
            lock.unlock();
        }
        return Result.ok(SeckillStateEnum.SUCCESS);
    }

    @Override
    public Result SkAopLock(long skId, long userId) {
        return null;
    }

    @Override
    public Result SkDBPCC(long skId, long userId, long number) {
        return null;
    }

    @Override
    public Result skDBOCC(long skId, long userId, long number) {
        return null;
    }
}
