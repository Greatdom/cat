package com.wdd.cat.service;

import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.service.tool.DecisionContext;
import org.springframework.stereotype.Component;

/**
 * &#064program: cat
 * <p>
 * &#064description: 反射链路
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 12:33
 **/
@Component
public class ReflexService {

    /** 处理一条走反射的消息，产出直接交给 PostService。 */
    public void run(InboundMessage message, DecisionContext ctx) {
        // TODO 命中反射规则 → 直接产回复 → 交给 PostService
    }
}
