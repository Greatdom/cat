package com.wdd.cat.message.child;


import com.wdd.cat.ego.EgoDecision;

/**
 * &#064program: cat
 * <p>
 * &#064description: 入站消息
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-17 11:58
 **/

public class InboundMessage extends NormalMessage {
    //TODO 根据业务拓展字段
    private RecallBundle recallBundle;   // 本轮情境
    private Assessment assessment;       // 对消息的理解
    private EgoDecision egoDecision;     // 自我决定

    //TODO 重写string,equals和hashcode方法

}