package kiruto.rpcserver.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import doraemon.entity.Result;
import doraemon.service.SkService;
import kiruto.annotation.RpcService;
import kiruto.rpcserver.anno.ServiceLock;
import kiruto.rpcserver.entity.SeckillComd;
import kiruto.rpcserver.entity.SqlVo;
import kiruto.rpcserver.entity.SuccessKilled;
import kiruto.rpcserver.enums.SeckillStateEnum;
import kiruto.rpcserver.exception.SkException;
import kiruto.rpcserver.mapper.SeckillComdMapper;
import kiruto.rpcserver.mapper.SuccesskilledMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = Exception.class)
    public Result skLock(long skId, long userId) {
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

    /**
     * 用注解，基本就是串行了.
     */
    @Override
    @ServiceLock
    @Transactional(rollbackFor = Exception.class)
    public Result skAopLock(long skId, long userId) {
        // 整个已经被try、lock包围了
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
        return Result.ok(SeckillStateEnum.SUCCESS);
    }

    /**
     * update后面的条件取得是范围，而且没走索引，会锁表.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result skDBPCC(long skId, long userId) {
        String sql = "update seckill set number = number - 1 where seckill_id = %s and number > 0";
        int count = seckillComdMapper.updateSql(new SqlVo(String.format(sql, skId)));
        // count > 0, 更新成功
        if (count > 0) {
            SuccessKilled successKilled = new SuccessKilled();
            successKilled.setSeckillId(skId);
            successKilled.setUserId(userId);
            // 这里并不知道数量，也没必要查
            successKilled.setState((short) 0);
            successKilled.setCreateTime(new Timestamp(System.currentTimeMillis()));
            successkilledMapper.insert(successKilled);
            return Result.ok(SeckillStateEnum.SUCCESS);
        } else {
            return Result.error(SeckillStateEnum.END);
        }
    }

    /**
     * 每次查询都会差全部数据，或者说至少带上版本，如果版本对上了，就能更新，
     * 如果没对上，说明在这之前已经有了别的更新, 此时无法再进行更新.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result skDBOCC(long skId, long userId) {
        // 可能为空
        QueryWrapper<SeckillComd> wrapper = new QueryWrapper<>();
        wrapper.eq("seckill_id", skId);
        SeckillComd seckillComd = seckillComdMapper.selectOne(wrapper);
        if (seckillComd != null && seckillComd.getNumber() >= 1) {
            String sql = "update seckill set number = number - 1 where seckill_id = %s and version = %s";
            int count = seckillComdMapper.updateSql(new SqlVo(String.format(sql, skId, seckillComd.getVersion())));
            if (count > 0) {
                SuccessKilled successKilled = new SuccessKilled();
                successKilled.setSeckillId(skId);
                successKilled.setUserId(userId);
                successKilled.setState((short) 0);
                successKilled.setCreateTime(new Timestamp(System.currentTimeMillis()));
                successkilledMapper.insert(successKilled);
                return Result.ok(SeckillStateEnum.SUCCESS);
            } else {
                return Result.error(SeckillStateEnum.END);
            }
        } else {
            return Result.error(SeckillStateEnum.END);
        }
    }

}
