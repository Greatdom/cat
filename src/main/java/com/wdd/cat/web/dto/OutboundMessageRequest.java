package com.wdd.cat.web.dto;

import java.util.List;

/**
 * &#064program: cat
 * &#064description: POST /api/outbound_message/push 的请求体：跳板把这一轮的回复推回来
 * <p>
 * 跳板只给「说了什么」，来源和会话由 Java 按 archiveId / sessionId 反查补全。
 * 第一版只收纯文本；图片等结构化内容等 Content 的多态序列化定了再加。
 * &#064author: black-cat
 * &#064create: 2026-11-19 19:56
 **/
public record OutboundMessageRequest(
        String archiveId,      // 哪个存档说的，雪花 id 字符串
        String sessionId,      // 哪间 DSH 会话
        InboundRef inbound,    // 这一轮在回的那条入站消息
        List<Item> messages    // 这一轮要发出去的话
) {

    /** 入站消息的身份：够 Java 定位渠道、目标、原话就行。 */
    public record InboundRef(
            String archiveId,
            String sessionId,
            String channel,          // "external_qq"
            String chatType,         // "group" / "private"
            String chatId,
            String senderId,
            String senderName,
            String card,
            String senderKind,       // HUMAN / BOT / SYSTEM
            String channelMessageId,
            long timestamp,
            String text              // 原话，回顾时要用
    ) {}

    /** 一条回复；chatType / chatId 留空表示沿用入站消息的。 */
    public record Item(
            String chatType,
            String chatId,
            String text
    ) {}
}
