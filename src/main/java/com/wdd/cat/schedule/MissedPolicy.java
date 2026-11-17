package com.wdd.cat.schedule;


import com.wdd.cat.common.enums.MissedPolicyKind;

/**
 * &#064program: cat
 * <p>
 * &#064description: 错过计划时的策略
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 18:59
 **/


public interface MissedPolicy {
    MissedPolicyKind kind();
    void handle(ScheduleItem item, long lateByMs);
}