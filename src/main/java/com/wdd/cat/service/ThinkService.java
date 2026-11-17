package com.wdd.cat.service;


import com.wdd.cat.processor.Processor;
import com.wdd.cat.processor.pre_processor.PreProcessor;
import com.wdd.cat.processor.think_processer.ThinkProcessor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 思考链路
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 12:27
 **/
@Component
public class ThinkService {

    private final List<ThinkProcessor> thinkProcessors;

    public ThinkService(List<ThinkProcessor> thinkProcessors) {
        this.thinkProcessors = new ArrayList<>(thinkProcessors);
    }

    @PostConstruct
    void checkProcessors() {
        thinkProcessors.forEach(Processor::validateKind);
        thinkProcessors.sort(Comparator.comparingInt(Processor::order));
    }



    //TODO将数据处理完后交给InboundMessageService


}
