package com.wdd.cat.state.environment;
import java.util.Map;
import java.util.Set;


/**
 * &#064program: cat
 * <p>
 * &#064description: ESP32 等硬件上报的环境更新;由 EnvironmentSystem 收下后转成 EnvironmentState
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 17:19
 **/


public record EnvironmentUpdate(
        String spaceId,                 // 哪个空间（硬件标识）
        Cell position,                  // 现在在哪
        Map<String, Double> ambience,   // 上报的环境量
        Set<String> nearby,             // 附近的道具 id（不含详细信息，要查表）
        String source                   // "esp32"
) {}