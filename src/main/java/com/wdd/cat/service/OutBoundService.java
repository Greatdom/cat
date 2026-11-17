package com.wdd.cat.service;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * &#064program: cat
 * <p>
 * &#064description: 出站业务
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 22:42
 **/
@Component
public class OutBoundService {

    //维护一个线程安全的消息容器用于缓存

    @Scheduled(fixedDelayString = "${cat.inbound_service.tick.ms:500}")
    void handle(){
        //遍历具体的sender,将缓存OutboundMessage拆成NormalMessage并按渠道送出去
    }

}
