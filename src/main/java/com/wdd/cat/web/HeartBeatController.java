package com.wdd.cat.web;

import com.wdd.cat.archive.Archive;
import com.wdd.cat.archive.ArchiveStore;
import com.wdd.cat.common.entity.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * &#064program: cat
 * &#064description: 汇报状态的Controller
 * &#064author: black-cat
 * &#064create: 2026-11-16 15:50
 **/
@RestController
@RequestMapping("/api")
public class HeartBeatController {

    /**
     * 跳板心跳
     */
    @PostMapping("/heartbeat")
    public Result<?> heartBeat() {
        return Result.ok();
        //TODO 参数是HeartBeatRequest(目前的内容物是List<String> sessionId),返回Result.ok()

        //TODO 第一版要将HeartBeatRequest的List<String> sessionId注入到AvailableSessions
    }

    /** 检测跳板和服务器是否连接成功。 */
    @GetMapping("/health")
    public Result<?> health() {
        return Result.ok("服务器运行中");
    }
}
