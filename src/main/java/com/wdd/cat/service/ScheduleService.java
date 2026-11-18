package com.wdd.cat.service;

import com.wdd.cat.common.utils.CatScheduler;
import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import com.wdd.cat.schedule.ScheduleItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * &#064description: 日程/欲望触发器
 * &#064author: black-cat
 * &#064create: 2026-11-16 16:03
 **/
@Component
public class ScheduleService {

    private final TriggerService triggerService;

    public ScheduleService(TriggerService triggerService, CatScheduler scheduler) {
        this.triggerService = triggerService;
        scheduler.schedule("schedule.date", UnEditAbleDefaultConfig.SCHEDULE_DATE_MS, this::date);
        scheduler.schedule("schedule.desire", UnEditAbleDefaultConfig.SCHEDULE_DESIRE_MS, this::desire);
    }

    public ScheduleItem create(ScheduleItem draft) {
        return null;   // TODO 生成计划
    }

    public void cancel(String id) {
        // TODO 取消计划
    }

    public List<ScheduleItem> list(long archiveId) {
        return null;   // TODO 列出计划
    }

    /** 日程到点就触发。 */
    void date() {
        // TODO 如果有日程触发就生成日程
    }

    /** 需求超过阈值就生成欲望。 */
    void desire() {
        // TODO 需求超过阈值会生成欲望
    }
}
