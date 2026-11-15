package com.wdd.cat.service;


import com.wdd.cat.prosessor.PreProcessor;
import org.springframework.beans.factory.annotation.Autowired;
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

    //业务流程:InboundService内有个tick方法,按固定时间轮询拉取所有消息
    //如果上一轮消息链表的前处理还没完毕,跳过,等下一轮重试
    //tick的轮询时间和各poller的轮询时间是独立的:
    // 例如.在tick方法设置轮询时间100ms,却在OneBotPoller的poll内调用间隔为2000ms的定时器,未满间隔就保持静默,
    //而在esp的poller里,这个定时器也是100ms,或者是弃用定时器,改用ws协议拉取
    //tick方法拉取到的消息后先进入前处理器HeadProcessor按策略处理,处理后将消息传给TriggerService继续处理

//    @Scheduled(fixedDelayString = "${cat.inbound.heartbeat-ms:2000}")

//    void tick() {
//        for (每个存档) {

    //        if(上一轮前处理还没完成){return;}
//            List<InboundMessage> chainInput = new ArrayList<>();
//
//            // ① 每个处理器先吐出自己的积压
//            for (PreProcessor p : sorted) {
//                chainInput.addAll(p.drain());
//            }
//
//            // ② 再拉新消息
//            if(chainInput为空)
//              chainInput.addAll(pollAndConvert());
//
//            // ③ 整条链跑一遍
//            for (PreProcessor p : sorted) {
//                chainInput = p.process(chainInput);
//            }
//        }
            //chainInput给triggerService继续处理
//    }

}
