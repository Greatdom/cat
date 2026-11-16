package com.wdd.cat.state.memory;


/**
 * &#064program: cat
 * <p>
 * &#064description: 长时记忆里的一条;它记的是"关于某人的某个事实",不是聊天记录本身;聊天记录在短时记忆里，长时记忆是提炼过的东西
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:31
 **/

import com.wdd.cat.common.enums.LongTermMemoryKind;

import java.util.Map;
import java.util.Set;

public record LongTermMemoryEntry(
        String id,                 // 唯一 id
        LongTermMemoryKind kind,           // 什么类型：事实/事件/情绪/关系/偏好/流程
        String content,            // 正文，一句话说完
        String subject,            // 关于谁："me" / "them:10002" / "world"
        double importance,         // 重要程度 0~1，检索打分要用
        double emotion,            // 情绪色彩 -1~1，负数=不愉快
        long createdAt,            // 什么时候记下的
        long lastUsedAt,           // 上次被召回是什么时候（越近越优先）
        String supersededBy,       // 被哪条取代了（null = 还有效）
        Set<String> tags,          // 标签，辅助检索
        Map<String, Object> extra  // 各实现自己塞的字段，不参与通用逻辑
) {}