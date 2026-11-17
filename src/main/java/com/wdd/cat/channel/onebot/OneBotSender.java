package com.wdd.cat.channel.onebot;


import com.wdd.cat.channel.ChannelSender;
import com.wdd.cat.common.enums.ChannelKind;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 将OneBot格式的消息发出去
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 22:32
 **/
@Component
public class OneBotSender implements ChannelSender {
    @Override
    public ChannelKind kind() {
        return null;
    }

    @Override
    public int send(long archiveId, String chatType, String chatId, List payloads) {
        return 0;
    }
}
