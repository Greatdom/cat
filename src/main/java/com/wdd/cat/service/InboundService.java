package com.wdd.cat.service;


import com.wdd.cat.prosessor.preprocessor.PreProcessor;
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

    @Autowired
    private List<PreProcessor> preProcessor;
    @Autowired
    private TriggerService triggerService;

    //handle方法从具体的poller拉到消息会先缓存到这里,供第一个前处理器拉取
//    list<InboundMessage> inboundMessageCache; 要线程安全的容器

    @Scheduled(fixedDelayString = "${cat.inbound_service.handle.ms:500}")
    void handle(){
        //遍历具体的poller拉取信息后往inboundMessageCache追加信息
    }
    @Scheduled(fixedDelayString = "${cat.inbound_service.preprocess.ms:500}")
    void preprocess(){
        //第一个前处理器从inboundMessageCache获取信息处理,后一个前处理器得到前一个处理器的处理结果继续加工,最后一个前处理器将处理结果交给TriggerService
        //list<InboundMessage> current = new list<>;
        // inboundMessageCache.drainTo(processed);
//        for(PreProcessor p:preProcessor){
//            List<InboundMessage> next = p.process(current);
//            current = (next == null) ? List.of() : next;
//        }
//        if (!current.isEmpty()) {
//            triggerService.trigger(current);
//        }
    }


}
