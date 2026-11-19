package com.wdd.cat.web;

import com.wdd.cat.common.cache.AvailableSessions;
import com.wdd.cat.common.entity.Result;
import com.wdd.cat.web.dto.HeartBeatRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * &#064program: cat
 * &#064description: 汇报状态的Controller
 * &#064author: black-cat
 * &#064create: 2026-11-16 15:50
 **/
@RestController
@RequestMapping("/api")
public class HeartBeatController {

    private static final Logger log = LoggerFactory.getLogger(HeartBeatController.class);

    private final AvailableSessions availableSessions;

    public HeartBeatController(AvailableSessions availableSessions) {
        this.availableSessions = availableSessions;
    }

    /** 跳板心跳 */
    @PostMapping("/heartbeat")
    public Result<?> heartBeat(@RequestBody(required = false) HeartBeatRequest req) {
        //TODO 第一版:报上来的会话清单整体覆盖服务端记录
        availableSessions.replace(req == null ? null : req.sessions());
        log.debug("心跳：可用会话 {} 个", availableSessions.size());
        return Result.ok();
    }

    /** 检测跳板和服务器是否连接成功。 */
    @GetMapping("/health")
    public Result<?> health() {
        return Result.ok("服务器运行中");
    }
}
