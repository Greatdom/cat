package com.wdd.cat.state.need;

import com.wdd.cat.common.enums.NeedKind;

/**
 * &#064program: cat
 * <p>
 * &#064description: 变成欲望的需求
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:38
 **/


public record Desire(
        NeedKind need,      // 哪个需求
        double intensity,   // 强度 0~1
        String reason       // 一句人话解释："很久没人跟我说话了"
) {}