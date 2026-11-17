package com.wdd.cat.service;


import com.wdd.cat.processor.pre_processor.PreProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 轮询原始外部消息并前处理的业务
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 22:42
 **/
@Component
public class InboundService {


//    list<InboundMessage> inboundMessageCache; 维护一个线程安全的容器

    @Scheduled(fixedDelayString = "${cat.tick.ms:500}")
    void handle(){
        //遍历具体的poller拉取NormalMessage并转为InboundMessage后往inboundMessageCache追加信息
    }


}
