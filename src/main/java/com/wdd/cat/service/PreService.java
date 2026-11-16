package com.wdd.cat.service;


import com.wdd.cat.processor.pre_processor.PreProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 前处理链路
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 12:41
 **/
@Component
public class PreService {

    @Autowired
    private InboundService inboundService;
    @Autowired
    private List<PreProcessor> preProcessor;
    @Autowired
    private TriggerService triggerService;

    @Scheduled(fixedDelayString = "${cat.inbound_service.tick.ms:500}")
    void preprocess(){
        //第一个前处理器从inboundService的inboundMessageCache获取信息处理
        // 后一个前处理器得到前一个处理器的处理结果继续加工,最后一个前处理器将处理结果交给TriggerService
        //list<InboundMessage> current = new list<>;
        // inboundService.inboundMessageCache.drainTo(processed);
//        for(PreProcessor p:preProcessor){
//            List<InboundMessage> next = p.process(current);
//            current = (next == null) ? List.of() : next;
//        }
//        if (!current.isEmpty()) {
//            triggerService.trigger(current);
//        }
    }


}
