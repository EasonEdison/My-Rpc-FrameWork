package doraemon.service;

import doraemon.entity.Result;

/**
 * 有两个表，一个是商品表，一个是订单表.
 */
public interface SkService {

    /**
     * 查询成功卖出商品数量.
     */
    Long getSuccessCount(long skId);

    /**
     * 删除成功卖出商品记录,为了方便调试，每次删完还要再加上数量到商品表.
     */
    void deleteSuccess(long skId);

    /**
     * 程序锁, 这是一种错误的实现，因为是先释放锁，再提交事物，所以下一个事务可能在上一个事务未提交时获取锁，读到老数据.
     */
    Result SkLock(long skId, long userId);

    /**
     * 程序锁AOP.
     */
    Result SkAopLock(long skId, long userId);

    /**
     * 数据库悲观锁.
     */
    Result SkDBPCC(long skId, long userId, long number);

    /**
     * 数据库乐观锁.
     */
    Result skDBOCC(long skId, long userId, long number);

}
