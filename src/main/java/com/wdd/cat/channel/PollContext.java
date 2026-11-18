package com.wdd.cat.channel;

import com.wdd.cat.archive.Archive;
import com.wdd.cat.message.ChatKey;

import java.util.Set;

/**
 * &#064program: cat
 * <p>
 * &#064description: 轮询上下文：告诉 Poller「用哪条连接、只拉哪些会话」
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:41
 **/
public record PollContext(
        long archiveId,        // 哪个存档要拉
        Archive.Link link,     // 用哪条连接（含 account / baseUrl / token）
        Set<ChatKey> chats,    // 只关心这些会话
        int limit              // 一次最多拉几条
) {

    /** 本存档这一侧的账号。 */
    public String account() {
        return link == null ? null : link.account();
    }
}
