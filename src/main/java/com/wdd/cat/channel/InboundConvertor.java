package com.wdd.cat.channel;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.message.Message;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 原始报文转换为InboundMessage的转换接口;T = 该通道的原始报文类型
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:35
 **/



public interface InboundConvertor<T> {
    ChannelKind kind();
    List<Message> convert(List<T> raw, ConvertContext context);
}