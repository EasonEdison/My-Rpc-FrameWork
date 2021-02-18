package kiruto.rpcserver.anno;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Scope
@Aspect
@Order(1)
public class LockAspect {

    private static Lock lock = new ReentrantLock(true);

    /**
     * Service层的切点.
     */
    @Pointcut("@annotation(kiruto.rpcserver.anno.ServiceLock)")
    public void lockAspect(){}

    /**
     * 让锁包围事务.
     */
    @Around("lockAspect()")
    public Object around(ProceedingJoinPoint joinPoint) {
        lock.lock();
        Object obj = null;
        try {
            // 执行
            obj = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException();
        } finally {
            lock.unlock();
        }
        return obj;
    }
}
