package com.wdd.cat.channel;


/**
 * &#064program: cat
 * <p>
 * &#064description: OutBoundMessage转换为原始报文的转换接口;P = 该通道的原始报文
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:36
 **/

import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.message.child.NormalMessage;

import java.util.List;

public interface OutboundConvertor<P> {
    ChannelKind kind();
    List<P> convert(NormalMessage normalMessage);
}