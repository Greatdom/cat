package com.wdd.cat.state.memory;


import com.wdd.cat.common.enums.ShortTermEntryKind;

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
        long at,                    // 发生时间戳
        ShortTermEntryKind kind,    // 谁说的/谁做的
        String content,             // 内容原文
        Map<String, Object> meta,   // 附加信息：I_ASKED 时存当时提议的参数
        boolean resolved            // 只对 I_ASKED 有意义：对方回应了没有
) {}