package com.wdd.cat.service;


import com.wdd.cat.message.InboundMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 从不同渠道得到消息后根据情境将消息送往不同链路处理
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 12:19
 **/
@Component
public class TriggerService {

    public void trigger(List<InboundMessage> messages){
        //遍历信息,根据消息性质进入不同链路
        // TODO 拿 DecisionContext → pathDecider.decide() → 按结果分派
        // TODO REFLEX     → reflexService.run(trigger,ctx)
        // TODO DELIBERATION → thinkService.run(trigger, ctx)
    }

}
