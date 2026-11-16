package com.wdd.cat.schedule.impl;


/**
 * &#064program: cat
 * <p>
 * &#064description:对于这个时钟实现:存档时间是主时钟,现实时间只是校准源;
 * 存档时间 += (现实经过的时间) × 流速系数
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:01
 **/


import com.wdd.cat.schedule.Clock;
import org.springframework.stereotype.Component;

@Component
public class ArchiveClock implements Clock {

    /** 现实时间。只给日志和调试用。 */
    public long realNow() {
        return System.currentTimeMillis();
    }

    /** 当前流速系数：0 = 暂停，1 = 同步，N > 1 = 加速。 */
    public double flowRate() {
        return 1.0;
    }

    /** 切换模式。 */
    public void setMode(TimeMode mode) {
        // TODO INDEPENDENT → 0 或 1（看开关）
        // TODO SYNCED → 1
        // TODO ACCELERATED → N
    }

    @Override
    public long nowEpochSecond() {
        return 0;
    }

    /**
     * 独立 — 关闭存档时时间暂停，不会错过任何计划
     * 同步 — 永远等于现实，服务器关机期间的会补做
     * 加速 — 存档一天 = 现实 1 小时，计划会密集触发
     */
    public enum TimeMode { INDEPENDENT, SYNCED, ACCELERATED }
}