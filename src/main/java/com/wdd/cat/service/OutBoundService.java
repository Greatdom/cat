package com.wdd.cat.service;

import com.wdd.cat.common.utils.CatScheduler;
import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import org.springframework.stereotype.Component;

/**
 * &#064program: cat
 * &#064description: 出站业务
 * &#064author: black-cat
 * &#064create: 2026-11-15 22:42
 **/
@Component
public class OutBoundService {

    // TODO 维护一个线程安全的消息容器用于缓存

    public OutBoundService(CatScheduler scheduler) {
        scheduler.schedule("outbound.handle", UnEditAbleDefaultConfig.TICK_MS, this::handle);
    }

    void handle() {
        // 遍历具体的 sender，将缓存 OutboundMessage 拆成 NormalMessage 并按渠道送出去
    }
}
