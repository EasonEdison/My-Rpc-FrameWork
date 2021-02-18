package kiruto.web;

import doraemon.entity.Result;
import doraemon.service.HelloWorld;
import doraemon.service.SkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kiruto.annotation.RpcReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Api("秒杀")
@Slf4j
@RestController
@RequestMapping("/seckill")
public class SkController {

    private int userNum = 1000;

    private static int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(
        corePoolSize, corePoolSize + 1, 10l, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(1000));


    @RpcReference(group = "JoJo", version = "1")
    private SkService skService;

    @RpcReference(version = "1", group = "JoJo")
    private HelloWorld helloWorld;

    @ApiOperation(value = "测试")
    @GetMapping("/test")
    public void test() {
        String hello = helloWorld.hello("ko no dio da !");
        System.out.println(hello);
    }

    @ApiOperation(value = "秒杀一: 程序锁")
    @PostMapping("/skLock")
    public Result skLock(long skId) {
        // 等待所有线程都执行结束
        CountDownLatch latch = new CountDownLatch(userNum);
        // 清空之前订单
        // 这一步成功了
        skService.deleteSuccess(skId);
        log.info("开始秒杀一...");
        // 创建多个线程
        for (int i = 0; i < userNum; i++) {
            long userId = i;
            Runnable task = () -> {
                Result result = skService.skLock(skId, userId);
                if (result.getMsg().equals("SUCCESS")) {
                    log.info("用户 {} 秒杀成功!", userId);
                }
                latch.countDown();
            };
            executor.execute(task);
        }
        try {
            latch.await();
            Long successCount = skService.getSuccessCount(skId);
            log.info("一共秒杀出 {} 件商品!", successCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }

    @ApiOperation(value = "秒杀二: AOP程序锁")
    @PostMapping("/skAopLock")
    public Result skAopLock(long skId) {
        CountDownLatch latch = new CountDownLatch(userNum);
        skService.deleteSuccess(skId);
        log.info("开始秒杀二...");
        for (int i = 0; i < userNum; i++) {
            long userId = i;
            Runnable task = () -> {
                Result result = skService.skAopLock(skId, userId);
                if (result.getMsg().equals("SUCCESS")) {
                    log.info("用户 {} 秒杀成功!", userId);
                }
                latch.countDown();
            };
            executor.execute(task);
        }
        try {
            latch.await();
            Long successCount = skService.getSuccessCount(skId);
            log.info("一共秒杀出 {} 件商品!", successCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }

    @ApiOperation(value = "秒杀三: 数据库悲观锁")
    @PostMapping("/skDBPCC")
    public Result skDBPCC(long skId) {
        CountDownLatch latch = new CountDownLatch(userNum);
        skService.deleteSuccess(skId);
        log.info("开始秒杀三...");
        for (int i = 0; i < userNum; i++) {
            long userId = i;
            Runnable task = () -> {
                Result result = skService.skDBPCC(skId, userId);
                if (result.getMsg().equals("SUCCESS")) {
                    log.info("用户 {} 秒杀成功!", userId);
                }
                latch.countDown();
            };
            executor.execute(task);
        }
        try {
            latch.await();
            Long successCount = skService.getSuccessCount(skId);
            log.info("一共秒杀出 {} 件商品!", successCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }

    @ApiOperation(value = "秒杀四: 数据库乐观锁")
    @PostMapping("/skDBOCC")
    public Result skDBOCC(long skId) {
        CountDownLatch latch = new CountDownLatch(userNum);
        skService.deleteSuccess(skId);
        log.info("开始秒杀三...");
        for (int i = 0; i < userNum; i++) {
            long userId = i;
            Runnable task = () -> {
                Result result = skService.skDBOCC(skId, userId);
                if (result.getMsg().equals("SUCCESS")) {
                    log.info("用户 {} 秒杀成功!", userId);
                }
                latch.countDown();
            };
            executor.execute(task);
        }
        try {
            latch.await();
            Long successCount = skService.getSuccessCount(skId);
            log.info("一共秒杀出 {} 件商品!", successCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }
}
