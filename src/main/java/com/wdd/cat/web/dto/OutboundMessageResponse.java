package com.wdd.cat.web.dto;

/**
 * &#064program: cat
 * &#064description: POST /api/outbound_message/push 的响应：这一轮收下了哪个会话
 * <p>
 * 一轮只回一条确认。跳板据此清掉该会话整轮的出站缓存 ——
 * 不再按「收下几条 NormalMessage」逐条确认，因为跳板那边也是整轮一份缓存。
 * <p>
 * 只要回包里带了 sessionId，跳板就认定整轮已被接收；否则保留并按失败处理。
 * 真正的发送重试由 Java 出站队列负责。
 * &#064author: black-cat
 * &#064create: 2026-11-21 11:40
 **/
public record OutboundMessageResponse(
        String sessionId
) {

    /** 收下了这一轮。 */
    public static OutboundMessageResponse accepted(String sessionId) {
        return new OutboundMessageResponse(sessionId);
    }
}
