package com.wdd.cat.common.enums;


/**
 * &#064program: cat
 * <p>
 * &#064description: 消息的紧急程度枚举
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 20:29
 **/


public enum Urgency {
    HIGH,      // 急，现在就处理
    NORMAL,    // 一般
    LOW,       // 不急,可以晚点回
    UNKNOWN    // 判不出来,兜底
}