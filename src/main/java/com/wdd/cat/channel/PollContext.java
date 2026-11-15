package com.wdd.cat.channel;
import com.wdd.cat.message.ChatKey;

import java.util.Set;


/**
 * &#064program: cat
 * <p>
 * &#064description: 轮询上下文：告诉 Poller「替哪个存档、用哪个账号、只拉哪些会话」
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:41
 **/


public record PollContext(
        long archiveId,        // 哪个存档要拉
        String account,        // 用哪个账号拉（本存档自己的号）
        Set<ChatKey> chats,    // 只关心这些会话
        int limit              // 一次最多拉几条
) {}
