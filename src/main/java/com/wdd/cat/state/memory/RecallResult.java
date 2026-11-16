package com.wdd.cat.state.memory;


import java.util.List;
import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: 长时记忆检索的结果;两套呈现：narrative 给模型看，entries 给代码用
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:31
 **/

public record RecallResult(
        String narrative,              // 拼好的自然语言文本，直接塞进提示词
        List<LongTermMemoryEntry> entries,     // 召回到的原始条目，代码里要判"有没有"用这个
        Map<String, Double> scores     // 每条的打分，只在调试/排障时看
) {
    /** 什么都没召回到。 */
    public static RecallResult empty() {
        // TODO new RecallResult("", List.of(), Map.of())
        return null;
    }
}