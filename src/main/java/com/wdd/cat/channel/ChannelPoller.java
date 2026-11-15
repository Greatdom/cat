package com.wdd.cat.channel;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.exception.PollException;

import java.util.List;


/**
 * &#064program: cat
 * <p>
 * &#064description: 拉取原始报文的接口;T = 该通道的原始报文类型。
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:33
 **/


public interface ChannelPoller<T> {
    ChannelKind kind();

    /** 拉一批。抛 PollException 表示这次拉失败，没拉到不等于没消息。 */
    List<T> poll(PollContext context) throws PollException;

    /** 建议的轮询间隔（毫秒）。讲通道自己扛得住多大压力：QQ 有风控 2000ms，ESP32 本机硬件 50ms。 */
    default int suggestedPollIntervalMs() { return 2000; }
}