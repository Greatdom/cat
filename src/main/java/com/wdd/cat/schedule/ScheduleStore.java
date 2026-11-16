package com.wdd.cat.schedule;


import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 计划的落盘。一个存档一个文件
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 18:58
 **/


@Component
public class ScheduleStore {
    public List<ScheduleItem> load(long archiveId){
        return null; //TODO
    }
    public void save(long archiveId, List<ScheduleItem> items){
        //TODO
    }
}