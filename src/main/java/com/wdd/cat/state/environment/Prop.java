package com.wdd.cat.state.environment;
import com.wdd.cat.state.memory.Entity;

import java.util.Map;
import java.util.Set;


/**
 * &#064program: cat
 * <p>
 * &#064description: 空间里的一件东西：灯、杯子、门、椅子
 * prop 是"东西"，terrain 是"地形"，两者不是一回事
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:34
 **/



public record Prop(
        Entity entity,              //实体
        String id,                  // "lamp-1"
        String name,                // "床头灯"
        String kind,                // "light" / "cup" / "door"
        Cell position,              // 摆在哪
        Map<String, Double> state,  // 当前状态：{"on": 0} / {"level": 0.8}
        Set<String> interactable    // 能做哪些动作："turn_on" / "turn_off" / "drink"
) {}