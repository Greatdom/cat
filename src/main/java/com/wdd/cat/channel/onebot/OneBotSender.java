package com.wdd.cat.channel.onebot;

import com.wdd.cat.channel.ChannelSender;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.exception.OneBotException;
import com.wdd.cat.message.child.NormalMessage;
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
 * 第三处限流（按存档分桶、每桶每次一条）的队列在后续实现里。
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

    @Override
    public int send(long archiveId, String chatType, String chatId, List<Map<String, Object>> payloads) {
        throw new UnsupportedOperationException("send 需要 client，见 send(client, ...)");
    }

    /** 用指定 client 发送；返回成功条数。 */
    public int send(OneBotClient client, String chatType, String chatId,
                    List<Map<String, Object>> payloads) {
        if (client == null || payloads == null || payloads.isEmpty()) {
            return 0;
        }
        String action = "group".equalsIgnoreCase(chatType) ? "send_group_msg" : "send_private_msg";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("group".equalsIgnoreCase(chatType) ? "group_id" : "user_id", chatId);
        body.put("message", payloads);

        try {
            client.call(action, body);
            return 1;
        } catch (OneBotException e) {
            log.warn("发送失败 {} {}：{}", chatType, chatId, e.getMessage());
            return 0;
        }
    }

    /** 发送单条 NormalMessage 的便捷方法。 */
    public int send(OneBotClient client, NormalMessage msg, OneBotOutboundConvertor convertor) {
        List<Map<String, Object>> payloads = convertor.convert(msg);
        return send(client, msg.chatType(), msg.chatId(), payloads);
    }
}
