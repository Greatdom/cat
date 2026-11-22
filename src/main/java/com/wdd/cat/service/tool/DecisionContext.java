package com.wdd.cat.service.tool;


import com.wdd.cat.common.enums.NeedKind;
import com.wdd.cat.state.environment.EnvironmentState;
import com.wdd.cat.state.memory.derivative_state.EmotionState;
import com.wdd.cat.state.physiology.PhysiologyState;

import java.util.List;
import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: 决定上下文,根据情境判断走哪条链路路需要的现场信息
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 12:24
 **/


public record DecisionContext(
        //四套状态系统（情绪/环境/生理/反射规则）
        EmotionState emotion,
        Map<NeedKind, Double> needs,
        EnvironmentState environment,
        PhysiologyState physiology,
        List<ReflexRule> reflexRules
) {}
