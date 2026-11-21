package com.wdd.cat.state.memory;


import com.wdd.cat.common.enums.ShortTermEntryKind;
import com.wdd.cat.message.Content;

import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: 短时记忆里的一条
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:32
 **/

public record ShortTermMemoryEntry(
        String id,                  // 唯一 id
        String title,              //记忆标题
        long at,                    // 发生时间戳
        ShortTermEntryKind kind,    // 谁说的/谁做的
        Content content,             // 记忆原文
        boolean resolved            // 只对 I_ASKED 有意义：对方回应了没有
) {}