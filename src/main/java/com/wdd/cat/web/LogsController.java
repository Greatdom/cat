package com.wdd.cat.web;


import com.wdd.cat.common.entity.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * &#064program: cat
 * <p>
 * &#064description: 日志页Controller
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:53
 **/
@RestController
@RequestMapping("/api/logs")
public class LogsController {

    @GetMapping
    public Result<MergedLogs> logs(){
        return null;
        //合并查询日志
    }

    public record MergedLogs(List<Map<String, Object>> entries, int count,
                             Map<String, Integer> sources, String hostLogFile) {
    }

}
