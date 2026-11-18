package com.wdd.cat.channel.onebot;

import com.wdd.cat.archive.Archive;
import com.wdd.cat.common.utils.JsonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * &#064program: cat
 * &#064description: OneBot 连接池；每个存档的每个 QQ 号一条连接
 * &#064author: black-cat
 * &#064create: 2026-11-15 22:03
 **/
@Component
public class OneBotConnectionPool {

    private static final Logger log = LoggerFactory.getLogger(OneBotConnectionPool.class);

    private static final int TIMEOUT_MS = 10_000;

    private final Map<String, OneBotClient> clients = new ConcurrentHashMap<>();
    private final JsonCodec json;

    public OneBotConnectionPool(JsonCodec json) {
        this.json = json;
    }

    /** 取或建。baseUrl/token 变了就重建。 */
    public OneBotClient forAccount(long archiveId, Archive.Link link) {
        String key = key(archiveId, link.account());
        OneBotClient existing = clients.get(key);
        if (existing != null && existing.matches(link.baseUrl(), link.token())) {
            return existing;
        }
        OneBotClient created = new OneBotClient(json, link.baseUrl(), link.token(), TIMEOUT_MS);
        clients.put(key, created);
        if (existing != null) {
            log.info("连接已重建：{}", key);
        }
        return created;
    }

    /** 存档删了/停用，清掉它的连接。 */
    public void evict(long archiveId) {
        String prefix = archiveId + ":";
        clients.keySet().removeIf(k -> k.startsWith(prefix));
    }

    /** 当前连接数，排障用。 */
    public int size() {
        return clients.size();
    }

    private static String key(long archiveId, String account) {
        return archiveId + ":" + (account == null ? "" : account);
    }
}
