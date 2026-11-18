package com.wdd.cat.service;

import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import com.wdd.cat.common.utils.CatScheduler;
import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.processor.Processor;
import com.wdd.cat.processor.pre_processor.PreProcessor;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * &#064program: cat
 * &#064description: 前处理链路
 * &#064author: black-cat
 * &#064create: 2026-11-16 12:41
 **/
@Component
public class PreService {

    private final InboundService inboundService;
    private final TriggerService triggerService;
    private final List<PreProcessor> preProcessors;

    public PreService(InboundService inboundService,
                      TriggerService triggerService,
                      List<PreProcessor> preProcessors,
                      CatScheduler scheduler) {
        this.inboundService = inboundService;
        this.triggerService = triggerService;
        this.preProcessors = new ArrayList<>(preProcessors);
        scheduler.schedule("pre.preprocess", UnEditAbleDefaultConfig.TICK_MS, this::preprocess);
    }

    @PostConstruct
    void checkProcessors() {
        preProcessors.forEach(Processor::validateKind);
        preProcessors.sort(Comparator.comparingInt(Processor::order));
    }

    /** 缓存池 → 前处理链 → 待投递队列。 */
    void preprocess() {
        List<InboundMessage> current = new ArrayList<>();
        inboundService.drainTo(current);
        if (current.isEmpty()) {
            return;
        }

        for (PreProcessor p : preProcessors) {
            List<InboundMessage> next = p.process(current);
            current = next == null ? List.of() : next;
            if (current.isEmpty()) {
                return;
            }
        }

        for (InboundMessage msg : current) {
            //TODO 将消息给triggerService处理
        }
    }
}
