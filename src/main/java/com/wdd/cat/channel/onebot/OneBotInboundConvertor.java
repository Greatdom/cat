package com.wdd.cat.channel.onebot;


import com.wdd.cat.channel.ConvertContext;
import com.wdd.cat.channel.InboundConvertor;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.message.Message;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 职责是将OneBot的原始信息转换成InboundMessage
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 22:29
 **/

public class OneBotInboundConvertor implements InboundConvertor {
    //TODO
    @Override
    public ChannelKind kind() {
        return null;
    }

    @Override
    public List<Message> convert(List raw, ConvertContext context) {
        return List.of();
    }
}
