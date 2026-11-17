package com.wdd.cat.service;


import com.wdd.cat.processor.Processor;
import com.wdd.cat.processor.post_processor.PostProcessor;
import com.wdd.cat.processor.think_processer.ThinkProcessor;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 后处理链路
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 12:34
 **/
@Component
public class PostService {

    private final List<PostProcessor> postProcessors;

    public PostService(List<PostProcessor> postProcessors) {
        this.postProcessors = new ArrayList<>(postProcessors);
    }

    @PostConstruct
    void checkProcessors() {
        postProcessors.forEach(Processor::validateKind);
        postProcessors.sort(Comparator.comparingInt(Processor::order));
    }


    //TODO 将消息从OutboundMessageController或者ReflexService接收后进行后处理,然后交给OutboundService

}
