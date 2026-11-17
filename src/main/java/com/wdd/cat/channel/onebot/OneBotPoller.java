package com.wdd.cat.channel.onebot;


import com.wdd.cat.channel.ChannelPoller;
import com.wdd.cat.channel.PollContext;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.exception.PollException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 从OneBot协议批量拉取信息
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 22:27
 **/
@Component
public class OneBotPoller implements ChannelPoller {
    //TODO
    @Override
    public ChannelKind kind() {
        return null;
    }

    @Override
    public List<?> poll(PollContext context) throws PollException {
        return List.of();
    }
}
