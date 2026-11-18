package com.wdd.cat.common.utils;

import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * &#064program: cat
 * &#064description: 统一调度器；周期直接读 UnEditAbleDefaultConfig 常量
 * &#064author: black-cat
 * &#064create: 2026-11-18 15:40
 **/
@Component
public class CatScheduler {

    private static final Logger log = LoggerFactory.getLogger(CatScheduler.class);

    private final ScheduledExecutorService executor =
            Executors.newScheduledThreadPool(UnEditAbleDefaultConfig.CAT_SCHEDULER_POOL_LIMIT, r -> {
                Thread t = new Thread(r, "cat-scheduler");
                t.setDaemon(true);
                return t;
            });

    /**
     * 按固定间隔重复执行。
     *
     * @param name   任务名，日志用
     * @param period 间隔（毫秒）
     * @param task   任务体
     */
    public void schedule(String name, long period, Runnable task) {
        executor.scheduleWithFixedDelay(() -> {
            try {
                task.run();
            } catch (RuntimeException e) {
                // 一个任务出错不该让后续调度停掉
                log.warn("任务 {} 执行失败：{}", name, e.getMessage(), e);
            }
        }, 0, period, TimeUnit.MILLISECONDS);
        log.info("已注册定时任务 {}，周期 {}ms", name, period);
    }

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }
}
