package com.wdd.cat.service;

import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.processor.Processor;
import com.wdd.cat.processor.think_processer.ThinkProcessor;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ThinkService.class);

    private final List<ThinkProcessor> thinkProcessors;

    public ThinkService(List<ThinkProcessor> thinkProcessors) {
        this.thinkProcessors = new ArrayList<>(thinkProcessors);
    }

    @PostConstruct
    void checkProcessors() {
        thinkProcessors.forEach(Processor::validateKind);
        thinkProcessors.sort(Comparator.comparingInt(Processor::order));
    }

    /** 跑一遍思考链，结果交给 InboundMessageController。 */
    public void run(List<InboundMessage> messages) {
        List<InboundMessage> current = messages;
        for (ThinkProcessor p : thinkProcessors) {
            List<InboundMessage> next = p.process(current);
            current = next == null ? List.of() : next;
            if (current.isEmpty()) {
                return;
            }
        }
        // TODO 交给 InboundMessageController 的待投递队列
        log.debug("思考链通过 {} 条", current.size());
    }
}
