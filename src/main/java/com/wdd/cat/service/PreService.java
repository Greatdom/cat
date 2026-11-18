package com.wdd.cat.service;

import com.wdd.cat.common.utils.CatScheduler;
import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import com.wdd.cat.processor.Processor;
import com.wdd.cat.processor.pre_processor.PreProcessor;
import jakarta.annotation.PostConstruct;
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

    void preprocess() {
        // 第一个前处理器从 inboundService 的缓存池取信息，后一个拿前一个的结果继续加工，
        // 最后一个把结果交给 TriggerService
    }
}
