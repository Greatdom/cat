package com.wdd.cat.channel.onebot;


import com.wdd.cat.channel.OutboundConvertor;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.message.child.NormalMessage;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 职责是将OutBoundMessage的消息转换成OneBot的消息
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 22:30
 **/

public class OneBotOutboundConvertor implements OutboundConvertor {
    //TODO
    @Override
    public ChannelKind kind() {
        return null;
    }

    @Override
    public List convert(NormalMessage normalMessage) {
        return List.of();
    }
}
