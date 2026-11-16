package com.wdd.cat.service;


import com.wdd.cat.message.InboundMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: description
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 22:46
 **/
@Component
public class TriggerService {


    void trigger(List<InboundMessage> message){
        //TODO 将数据进入缓存
    }


}
