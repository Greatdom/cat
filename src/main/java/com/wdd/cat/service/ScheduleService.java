package com.wdd.cat.service;


import com.wdd.cat.schedule.ScheduleItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 日程/欲望触发器
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:03
 **/
@Component
public class ScheduleService {

    public ScheduleItem create(ScheduleItem draft){
        return null;
        //生成计划
    }
    public void cancel(String id){
        //取消计划
    }
    public List<ScheduleItem> list(long archiveId){
        return null;
        //列出计划
    }

    @Autowired
    TriggerService triggerService;

    @Scheduled(fixedDelayString = "${cat.schedule_service.date.ms:20000}")
    void date(){
        //TODO 如果有日程触发就生成日程
        triggerService.trigger(null);
    }

    @Scheduled(fixedDelayString = "${cat.schedule_service.desire.ms:1800000}")
    void desire(){
        //TODO 需求超过阈值会生成欲望
        triggerService.trigger(null);
    }

}
