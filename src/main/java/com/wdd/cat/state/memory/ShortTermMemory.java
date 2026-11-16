package com.wdd.cat.state.memory;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 短时记忆：这个存档最近发生了什么（窗口内的一串条目）;让模型能接上话，而不用跳板自己记历史
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:32
 **/



public record ShortTermMemory(
        List<ShortTermMemoryEntry> entries,   // 窗口内的条目，按时间正序
        EmotionState emotion,           // 当前情绪（从这些条目推出来的）
        long windowStart,               // 窗口起点时间戳
        long windowEnd                  // 窗口终点时间戳
) {
    /** 我提过但对方还没回答的问题，需要一直惦记着。 */
    public List<ShortTermMemoryEntry> pendingAsks() {
        // TODO 筛 kind == I_ASKED && !resolved
        return List.of();
    }

    /** 渲染成提示词片段，塞进 DSH 的上下文。 */
    public String toPromptFragment() {
        // TODO 按时间顺序把 entries 拼成自然语言
        // TODO pendingAsks 单独提一段，提醒模型"你问过这个，还没回音"
        return "";
    }
}