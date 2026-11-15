package com.wdd.cat.common.enums;


/**
 * &#064program: cat
 * <p>
 * &#064description: 日程/计划的类型枚举
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 20:30
 **/

public enum ScheduleKind {
    ONCE,       // 一次性，用 at
    RECURRING   // 周期性，用 cron
}