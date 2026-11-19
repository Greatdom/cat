package com.wdd.cat.web.dto;

import java.util.List;

/**
 * &#064program: cat
 * &#064description: POST /api/outbound_message/push 的响应：确认哪些回复已经收下
 * <p>
 * 与入站 ack 一致：Java 收下即确认。跳板据此清掉自己的出站缓存，
 * 真正的发送重试由 Java 出站队列负责。
 * &#064author: black-cat
 * &#064create: 2026-11-19 20:58
 **/
public record OutboundMessageResponse(
        List<Item> items
) {

    /** 一条确认。 */
    public record Item(
            String archiveId,
            String sessionId,
            int index,
            String text
    ) {}
}