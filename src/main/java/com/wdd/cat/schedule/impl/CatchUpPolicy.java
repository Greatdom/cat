package com.wdd.cat.schedule.impl;

import com.wdd.cat.common.enums.MissedPolicyKind;
import com.wdd.cat.schedule.MissedPolicy;
import com.wdd.cat.schedule.ScheduleItem;
import org.springframework.stereotype.Component;


/**
 * &#064program: cat
 * <p>
 * &#064description: 补执行策略：照常触发，但标注"这是迟到的"
 * 适合"该做的事还得做"的场景，比如每天该吃的药
 * 补不补由 ScheduleItem.catchUpWindowMs 决定——超出窗口的仍然算错过
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:20
 **/

@Component
public class CatchUpPolicy implements MissedPolicy {

    @Override
    public MissedPolicyKind kind() {
        return MissedPolicyKind.CATCH_UP;
    }

    @Override
    public void handle(ScheduleItem item, long lateByMs) {
        // TODO 超时超过 item.catchUpWindowMs() → 转交给下一个策略（或不补）
        // TODO 没超 → 组 Trigger.Timer(archiveId, id)，payload 里标注 lateByMs
        // TODO 交给 TriggerBus，让下游知道"这不是准点的"
    }
}