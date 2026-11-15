package com.wdd.cat.channel;

import java.util.List;
import com.wdd.cat.common.enums.ChannelKind;

/**
 * &#064program: cat
 * <p>
 * &#064description: 将原始报文发出去;P = 该通道的原始报文
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:38
 **/


public interface ChannelSender<P> {
    ChannelKind kind();

    /** 发。返回发送成功的条数（可能因为拆条、部分失败而小于 payloads.size()）。 */
    int send(long archiveId, String chatType, String chatId, List<P> payloads);
}