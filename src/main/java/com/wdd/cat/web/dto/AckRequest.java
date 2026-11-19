package com.wdd.cat.web.dto;

import java.util.List;

/**
 * &#064program: cat
 * &#064description: POST /api/inbound_message/ack 的请求体
 * <p>
 * 跳板拿到消息后立刻 ack，此时 Java 侧标记 DELIVERED 并推进水位。
 * 跳板之后才按 sessionId 找 DSH 会话，找不到就自行丢弃并记日志。
 * &#064author: black-cat
 * &#064create: 2026-11-19 12:06
 **/
public record AckRequest(
        List<Item> items
) {

    /** 一条确认。 */
    public record Item(
            String archiveId,          // 雪花 id，字符串
            String sessionId,          // 归属的 DSH 会话
            String channelMessageId,   // 渠道消息号
            String chatKey,            // "external_qq:group:20001"
            long timestamp             // 消息时间戳（毫秒），用于推水位
    ) {}
}
