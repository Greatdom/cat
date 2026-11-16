package com.wdd.cat.ego;


import com.wdd.cat.state.memory.PersonalityView;
import org.springframework.scheduling.Trigger;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 超我引擎：根据当前这件事和性格，算出哪些超我规则在起作用
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 17:57
 **/


@Component
public class SuperegoEngine {

    public List<SuperegoRule> evaluate(Trigger t, PersonalityView p) {
        // TODO 从规则表里筛 condition 命中的
        // TODO 用 p 算 effectiveWeight，权重太低的不返回
        return List.of();
    }
}
