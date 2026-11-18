package com.wdd.cat.service;

import com.wdd.cat.common.enums.NeedKind;
import com.wdd.cat.common.enums.ThinkingPath;
import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.service.tool.DecisionContext;
import com.wdd.cat.service.tool.PathDecider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * &#064program: cat
 * &#064description: 从不同渠道得到消息后根据情境将消息送往不同链路处理
 * &#064author: black-cat
 * &#064create: 2026-11-16 12:19
 **/
@Component
public class TriggerService {

    private final PathDecider pathDecider;
    private final ReflexService reflexService;
    private final ThinkService thinkService;

    public TriggerService(PathDecider pathDecider,
                          ReflexService reflexService,
                          ThinkService thinkService) {
        this.pathDecider = pathDecider;
        this.reflexService = reflexService;
        this.thinkService = thinkService;
    }

    public void trigger(List<InboundMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        DecisionContext ctx = buildContext();
        for (InboundMessage msg : messages) {
            ThinkingPath path = pathDecider.decide(msg, ctx);
            switch (path) {
                case REFLEX -> reflexService.run(msg, ctx);
                case DELIBERATION -> thinkService.run(List.of(msg));
            }
        }
    }

    /**
     * 第一版的判断上下文：四套状态系统都还没有实现，先给空值。
     * <p>等情绪/需求/环境/生理/反射规则就绪后，这里改成真正去取。
     */
    private DecisionContext buildContext() {
        return new DecisionContext(
                null,               // EmotionState 未实现
                Map.<NeedKind, Double>of(),
                null,               // EnvironmentState 未实现
                null,               // PhysiologyState 未实现
                List.of()           // 还没有反射规则表
        );
    }
}
