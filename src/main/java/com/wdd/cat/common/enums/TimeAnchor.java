package com.wdd.cat.common.enums;


/**
 * &#064program: cat
 * <p>
 * &#064description: 日程的时间用哪个方案解释和作为标准的枚举
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 20:31
 **/

public enum TimeAnchor {
    WALL_CLOCK,   // 跟现实时间（"每天现实 22:00"）
    ARCHIVE       // 跟存档时间（加速模式下"存档的 22:00"）
}