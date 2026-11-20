package com.wdd.cat.channel;

import com.wdd.cat.archive.Archive;

/**
 * &#064program: cat
 * <p>
 * &#064description: 发送上下文：告诉 Sender「用哪条连接、发给谁」
 * <p>
 * 和 PollContext 对称：渠道需要的一切都在这里，Service 不碰渠道细节。
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-20 21:58
 **/
public record SendContext(
        long archiveId,        // 哪个存档要发
        Archive.Link link,     // 用哪条连接（含 account / baseUrl / token）
        String chatType,       // "group" / "private"
        String chatId          // 发给哪个会话
) {

    /** 本存档这一侧的账号。 */
    public String account() {
        return link == null ? null : link.account();
    }
}
