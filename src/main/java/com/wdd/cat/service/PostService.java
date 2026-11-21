package com.wdd.cat.service;


import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import com.wdd.cat.common.utils.CatScheduler;
import com.wdd.cat.message.child.OutboundMessage;
import com.wdd.cat.processor.Processor;
import com.wdd.cat.processor.post_processor.PostProcessor;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    /** 缓存池 */
    private final ConcurrentLinkedQueue<OutboundMessage> outboundMessageCache = new ConcurrentLinkedQueue<>();

    private final List<PostProcessor> postProcessors;
    private final OutBoundService outBoundService;

    public PostService(List<PostProcessor> postProcessors,
                       OutBoundService outBoundService,
                       CatScheduler scheduler) {
        this.postProcessors = new ArrayList<>(postProcessors);
        this.outBoundService = outBoundService;
        scheduler.schedule("post.handle", UnEditAbleDefaultConfig.TICK_MS, this::handle);
    }

    @PostConstruct
    void checkProcessors() {
        postProcessors.forEach(Processor::validateKind);
        postProcessors.sort(Comparator.comparingInt(Processor::order));
    }

    void handle() {
        List<OutboundMessage> current = new ArrayList<>();
        OutboundMessage msg;
        while ((msg = outboundMessageCache.poll()) != null) {
            current.add(msg);
        }

        // 即使没有新消息也要把链跑完：有状态的处理器靠每轮心跳吐出积压
        for (PostProcessor p : postProcessors) {
            List<OutboundMessage> next = p.process(current);
            current = next == null ? List.of() : next;
        }

        if (current.isEmpty()) {
            return;
        }
        for (OutboundMessage m : current) {
            outBoundService.enqueue(m);
        }
        log.info("后处理通过 {} 条", current.size());
    }

    public void enqueue(OutboundMessage message) {
        if (message != null) {
            outboundMessageCache.add(message);
        }
    }
}
