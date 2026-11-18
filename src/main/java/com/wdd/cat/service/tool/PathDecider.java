package com.wdd.cat.service.tool;

import com.wdd.cat.common.enums.ThinkingPath;
import com.wdd.cat.message.child.InboundMessage;
import org.springframework.stereotype.Component;

/**
 * &#064program: cat
 * <p>
 * &#064description: 决定这条消息走反射还是思考
 * <p>
 * 第一版不判，恒走思考。ctx 现在是空壳，不校验合法性；
 * 等四套状态系统就绪后再用 ctx 做真正的判断。
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 12:25
 **/
@Component
public class PathDecider {

    public ThinkingPath decide(InboundMessage inboundMessage, DecisionContext ctx) {
        return ThinkingPath.DELIBERATION;
    }
}
