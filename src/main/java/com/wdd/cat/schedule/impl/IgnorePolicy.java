package com.wdd.cat.schedule.impl;


/**
 * &#064program: cat
 * <p>
 * &#064description: 直接丢弃
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:27
 **/

import com.wdd.cat.common.enums.MissedPolicyKind;
import com.wdd.cat.schedule.MissedPolicy;
import com.wdd.cat.schedule.ScheduleItem;
import org.springframework.stereotype.Component;

@Component
public class IgnorePolicy implements MissedPolicy {

    @Override
    public MissedPolicyKind kind() {
        return MissedPolicyKind.IGNORE;
    }

    @Override
    public void handle(ScheduleItem item, long lateByMs) {
        //留空
    }
}