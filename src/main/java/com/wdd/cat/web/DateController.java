package com.wdd.cat.web;


import com.wdd.cat.common.entity.Result;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * &#064program: cat
 * <p>
 * &#064description: 日程生成控制器
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 16:09
 **/
@Component
@RequestMapping("/api/date")
@Deprecated
public class DateController {

    @PostMapping
    public Result<?> add(Object o){
        return null;
        //TODO 生成日程并返回ack
    }

}
