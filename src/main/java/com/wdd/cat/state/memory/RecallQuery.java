package com.wdd.cat.state.memory;


/**
 * &#064program: cat
 * <p>
 * &#064description:   长时记忆的检索条件
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:31
 **/

import java.util.Set;


public record RecallQuery(
        String text,              // 拿什么去搜（消息原文）
        Set<String> topicHints,   // 话题提示，用来扩大检索范围
        String aboutWhom,         // 只关心关于谁的记忆（联系人 ID），可为空
        int maxEntries,           // 最多返回几条
        int maxChars              // 返回内容的总字数上限
) {}