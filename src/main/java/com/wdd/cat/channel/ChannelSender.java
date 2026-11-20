package com.wdd.cat.channel;

import com.wdd.cat.common.enums.ChannelKind;

/**
 * &#064program: cat
 * <p>
 * &#064description: 将原始报文发出去;P = 该通道的原始报文
 * <p>
 * 和 ChannelPoller 对称：连接地址从 {@link SendContext#link()} 取，
 * 实现不依赖 ArchiveStore —— 那些是 Service 的事。
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:38
 **/


public interface ChannelSender<P> {
    ChannelKind kind();

    /** 发一批消息段；返回发送成功的条数。 */
    int send(SendContext context, java.util.List<P> payloads);

    /**
     * 这个渠道建议的发消息间隔（毫秒）。
     * <p>和 {@link ChannelPoller#suggestedPollIntervalMs()} 对称：渠道自己最清楚
     * 能扛住多大的压力。QQ 有风控 2000ms，ESP32 本机硬件 50ms。
     * <p>OutBoundService 按渠道分桶，每桶按这里的节奏各取一条发出去。
     */
    default int suggestedSendIntervalMs() { return 2000; }
}
