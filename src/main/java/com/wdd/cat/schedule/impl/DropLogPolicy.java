package com.wdd.cat.schedule.impl;

import com.wdd.cat.common.enums.MissedPolicyKind;
import com.wdd.cat.schedule.MissedPolicy;
import com.wdd.cat.schedule.ScheduleItem;
import org.springframework.stereotype.Component;

/**
 * &#064program: cat
 * <p>
 * &#064description: 丢弃 + 记账：这次就算了，但记一笔"我错过了"
 * 适合一次性的、错过就没意义的计划（"提醒他 3 点开会"）
 * 写进记忆是为了让存档知道自己错过过——会有一点愧疚或遗憾
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:22
 **/

@Component
public class DropLogPolicy implements MissedPolicy {

    @Override
    public MissedPolicyKind kind() {
        return MissedPolicyKind.DROP_LOG;
    }

    @Override
    public void handle(ScheduleItem item, long lateByMs) {
        // TODO 不触发任何行为
        // TODO 写一条长时记忆：MemoryKind.EVENT，内容是"我错过了……"
        // TODO 记忆的 subject 填 "me"，emotion 偏低（遗憾）
    }
}