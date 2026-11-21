package com.wdd.cat.web;

import com.wdd.cat.common.cache.AvailableSessions;
import com.wdd.cat.common.entity.Result;
import com.wdd.cat.log.LinkerLogReader;
import com.wdd.cat.log.LogBuffer;
import com.wdd.cat.web.dto.HeartBeatRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    private static final Logger log = LoggerFactory.getLogger(HeartBeatController.class);

    private final AvailableSessions availableSessions;
    private final LinkerLogReader linkerLogs;

    public HeartBeatController(AvailableSessions availableSessions, LinkerLogReader linkerLogs) {
        this.availableSessions = availableSessions;
        this.linkerLogs = linkerLogs;
    }

    /** 跳板心跳：会话清单和跳板日志都是整体覆盖。 */
    @PostMapping("/heartbeat")
    public Result<?> heartBeat(@RequestBody(required = false) HeartBeatRequest req) {
        availableSessions.replace(req == null ? null : req.sessions());
        if (req != null && req.logs() != null) {
            linkerLogs.replace(toEntries(req.logs()));
        }
        log.debug("心跳：可用会话 {} 个，跳板日志 {} 条",
                availableSessions.size(), linkerLogs.size());
        return Result.ok();
    }

    private static List<LogBuffer.Entry> toEntries(List<HeartBeatRequest.LogEntry> incoming) {
        List<LogBuffer.Entry> out = new ArrayList<>(incoming.size());
        for (HeartBeatRequest.LogEntry e : incoming) {
            if (e == null) {
                continue;
            }
            out.add(new LogBuffer.Entry(
                    e.time(),
                    e.level() == null ? "INFO" : e.level(),
                    e.chatKey(),
                    e.itemId(),
                    e.logger() == null ? "linker" : e.logger(),
                    e.text() == null ? "" : e.text()));
        }
        return out;
    }

    /** 检测跳板和服务器是否连接成功 */
    @GetMapping("/health")
    public Result<?> health() {
        return Result.ok("服务器运行中");
    }
}
