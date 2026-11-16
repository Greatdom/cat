package com.wdd.cat.web;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * &#064program: cat
 * <p>
 * &#064description: 专门用于按具体会话分桶以队列限流投递消息之跳板并得到ack消息进而更新消息水位的Controller
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 09:44
 **/
@RestController
@RequestMapping("/api/inbound_message")
public class InboundMessageController {

    //TODO 按具体会话注入一个缓存队列群以限流,每次被要求投递是每个队列只能吐出一个消息

    //TODO投递接口

    //TODO ack接口

}
