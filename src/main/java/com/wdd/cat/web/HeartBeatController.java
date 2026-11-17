package com.wdd.cat.web;


import com.wdd.cat.common.entity.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * &#064program: cat
 * <p>
 * &#064description: 汇报状态的Controller
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 15:50
 **/
@RestController
@RequestMapping("/api")
public class HeartBeatController {

    @GetMapping("/heartbeat")
    public Result<?> heartBeat() {
        return null;//TODO 跳板每隔一段时间汇报当前状态,包括跳板设置/日志/会话信息/渠道能力
    }
    @GetMapping("/health")
    public Result<?> health() {
        return null;//TODO 检测跳板和服务器是否连接成功
    }

}
