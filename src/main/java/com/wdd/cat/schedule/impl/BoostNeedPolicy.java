package com.wdd.cat.schedule.impl;



import com.wdd.cat.schedule.MissedPolicy;
import com.wdd.cat.schedule.ScheduleItem;
import org.springframework.stereotype.Component;

/**
 * &#064program: cat
 * <p>
 * &#064description: 不补执行，把这个账算到需求头上
 * 比如"今天没跟人说话"的计划错过了，就不补发消息
 * 而是把 SOCIAL 需求推高一点——之后自然会想说话
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:22
 **/

@Component
public class BoostNeedPolicy implements MissedPolicy {

    @Override
    public String kind() {
        return "boost-need";
    }

    @Override
    public void handle(ScheduleItem item, long lateByMs) {
        // TODO item.forNeed() 为空 → 什么也不做（没挂钩需求，无账可算）
        // TODO 不为空 → needPool.boost(item.forNeed(), 一个按迟到时长算的量)
    }
}