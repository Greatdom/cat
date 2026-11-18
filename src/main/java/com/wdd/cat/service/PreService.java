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

    private static final Logger log = LoggerFactory.getLogger(PreService.class);

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

    /**
     * 缓存池 → 前处理链 → TriggerService。
     * <p>关键：即使没有新消息也要把整条链跑完。
     * 有状态的处理器（如合并）靠每轮心跳吐出积压，提前返回会让积压永远出不来。
     */
    void preprocess() {
        List<InboundMessage> current = new ArrayList<>();
        inboundService.drainTo(current);

        for (PreProcessor p : preProcessors) {
            List<InboundMessage> next = p.process(current);
            current = next == null ? List.of() : next;
        }

        if (current.isEmpty()) {
            return;
        }
        triggerService.trigger(current);
        log.info("前处理通过 {} 条", current.size());
    }
}
