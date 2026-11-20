package com.wdd.cat.channel.onebot;

import com.wdd.cat.channel.ChannelSender;
import com.wdd.cat.channel.SendContext;
import com.wdd.cat.common.config.EditAbleDefaultConfig;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.exception.OneBotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * &#064program: cat
 * &#064description: 把 OneBot 消息段数组发出去
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 22:32
 **/
@Component
public class OneBotSender implements ChannelSender<Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(OneBotSender.class);

    private final OneBotConnectionPool pool;

    public OneBotSender(OneBotConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    public ChannelKind kind() {
        return ChannelKind.EXTERNAL_QQ;
    }

    /** 发消息间隔和拉取间隔绑在一起：同一个 QQ 号，收发的风控是一体的。 */
    @Override
    public int suggestedSendIntervalMs() {
        return (int) EditAbleDefaultConfig.qqPollAndSendIntervalMs;
    }

    @Override
    public int send(SendContext context, List<Map<String, Object>> payloads) {
        if (context == null || context.link() == null || payloads == null || payloads.isEmpty()) {
            return 0;
        }
        OneBotClient client = pool.forAccount(context.archiveId(), context.link());

        boolean group = "group".equalsIgnoreCase(context.chatType());
        String action = group ? "send_group_msg" : "send_private_msg";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(group ? "group_id" : "user_id", context.chatId());
        body.put("message", payloads);

        try {
            client.call(action, body);
            return 1;
        } catch (OneBotException e) {
            log.warn("发送失败 {} {}：{}", context.chatType(), context.chatId(), e.getMessage());
            return 0;
        }
    }
}
