package com.wdd.cat.service.tool;

import com.wdd.cat.common.enums.ThinkingPath;
import com.wdd.cat.message.child.NormalMessage;
import org.springframework.stereotype.Component;


/**
 * &#064program: cat
 * <p>
 * &#064description: 决定这条消息走反射还是思考;第一版只判两种：反射规则命中、寒暄语。其余全走 DELIBERATION
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 12:25
 **/


@Component
public class PathDecider {

    public ThinkingPath decide(NormalMessage normalMessage, DecisionContext ctx) {
        // TODO 第一版直接返回 DELIBERATION，先跑通链路
        return ThinkingPath.DELIBERATION;
    }
}