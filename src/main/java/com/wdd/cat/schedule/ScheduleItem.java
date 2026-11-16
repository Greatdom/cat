package com.wdd.cat.schedule;

import com.wdd.cat.common.enums.NeedKind;
import com.wdd.cat.common.enums.ScheduleKind;
import com.wdd.cat.common.enums.TimeAnchor;

/**
 * &#064program: cat
 * <p>
 * &#064description: 一条计划：到点该做什么
 * 计划能挂钩需求,例如:白天已经聊够了，今晚这条问候就可以跳过
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 18:54
 **/



public record ScheduleItem(
        String id,
        long archiveId,             // 哪个存档的
        ScheduleKind kind,          // ONCE 一次性 / RECURRING 周期
        String at,                  // ONCE 用，ISO 时间
        String cron,                // RECURRING 用，cron 表达式
        NeedKind forNeed,           // 关联哪个需求（可空） 这就是"有意图"
        String payload,             // 到点什么（触发时用）
        TimeAnchor anchor,          // WALL_CLOCK 跟现实钟 / ARCHIVE 跟存档钟
        long catchUpWindowMs,       // 迟到多久内还算"补做有效"
        boolean enabled,            // 开关
        long lastFiredAt            // 上次触发时间，防重复触发
) {}