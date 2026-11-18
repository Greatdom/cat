package com.wdd.cat.channel.onebot;

import com.wdd.cat.archive.Archive;
import com.wdd.cat.channel.ChannelPoller;
import com.wdd.cat.channel.PollContext;
import com.wdd.cat.common.config.EditAbleDefaultConfig;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.exception.OneBotException;
import com.wdd.cat.exception.PollException;
import com.wdd.cat.message.ChatKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * &#064program: cat
 * &#064description: 从 OneBot 后端按会话批量拉取历史消息
 * &#064author: black-cat
 * &#064create: 2026-11-15 22:27
 **/
@Component
public class OneBotPoller implements ChannelPoller<JsonNode> {

    private static final Logger log = LoggerFactory.getLogger(OneBotPoller.class);

    /** 每个账号上次拉取时间，用于控频。 */
    private final Map<String, Long> lastPollAt = new ConcurrentHashMap<>();

    private final OneBotConnectionPool pool;

    public OneBotPoller(OneBotConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    public ChannelKind kind() {
        return ChannelKind.EXTERNAL_QQ;
    }

    @Override
    public int suggestedPollIntervalMs() {
        return (int) EditAbleDefaultConfig.qqPollAndSendIntervalMs;
    }

    /**
     * 从连接池按 link 取 client 再拉。
     * <p>link 由 {@link #poll(Archive.Link, PollContext)} 提供；直接用本方法会抛异常。
     */
    @Override
    public List<JsonNode> poll(PollContext context) throws PollException {
        throw new PollException("OneBotPoller 需要 link 才能定位账号，请用 poll(link, context)");
    }

    /** 按 link 从连接池取 client 拉一批。 */
    public List<JsonNode> poll(Archive.Link link, PollContext context) throws PollException {
        if (!due(context)) {
            return List.of();
        }
        OneBotClient client = pool.forAccount(context.archiveId(), link);
        return fetch(client, context);
    }



    /** 到轮询间隔了吗？到了就记下这次时间。 */
    private boolean due(PollContext context) {
        String accountKey = context.archiveId() + ":" + context.account();
        long now = System.currentTimeMillis();
        Long last = lastPollAt.get(accountKey);
        if (last != null && now - last < suggestedPollIntervalMs()) {
            return false;
        }
        lastPollAt.put(accountKey, now);
        return true;
    }

    private List<JsonNode> fetch(OneBotClient client, PollContext context) throws PollException {
        if (context.chats() == null || context.chats().isEmpty()) {
            return List.of();
        }
        List<JsonNode> out = new ArrayList<>();
        try {
            for (ChatKey chat : context.chats()) {
                out.addAll(history(client, chat, context.limit()));
            }
        } catch (OneBotException e) {
            throw new PollException("拉取失败：" + e.getMessage(), e);
        }
        return out;
    }

    private List<JsonNode> history(OneBotClient client, ChatKey chat, int limit) {
        String action = chat.isGroup() ? "get_group_msg_history" : "get_friend_msg_history";
        Map<String, Object> payload = chat.isGroup()
                ? Map.of("group_id", chat.id(), "count", limit)
                : Map.of("user_id", chat.id(), "count", limit);

        JsonNode data = client.call(action, payload);
        JsonNode messages = data.path("messages");
        if (!messages.isArray()) {
            return List.of();
        }
        List<JsonNode> out = new ArrayList<>(messages.size());
        messages.forEach(out::add);
        log.debug("{} 拉到 {} 条", chat.wire(), out.size());
        return out;
    }
}
