package com.wdd.cat.common.enums;


/**
 * &#064program: cat
 * <p>
 * &#064description: ProcessorKind
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-17 23:04
 **/

public enum ProcessorKind {
    DEDUP_PROCESSOR(100, ProcessorFamily.PRE_PROCESSOR),
    MERGE_PROCESSOR(200, ProcessorFamily.PRE_PROCESSOR),
    UNDERSTAND_PROCESSOR(100, ProcessorFamily.THINK_PROCESSOR),
    RECALL_PROCESSOR(200, ProcessorFamily.THINK_PROCESSOR),
    EGO_PROCESSOR(300, ProcessorFamily.THINK_PROCESSOR),
    SLIM_PROCESSOR(400, ProcessorFamily.THINK_PROCESSOR),
    EXPRESS_PROCESSOR(100, ProcessorFamily.POST_PROCESSOR),
    OBSERVE_PROCESSOR(200, ProcessorFamily.POST_PROCESSOR);
    //TODO 还有没实现的,例如INTERNAL_MEMORY ESP32 WECHAT EXTERNAL_AGENT

    private final int order;

    private final ProcessorFamily family;

    ProcessorKind(int order,ProcessorFamily family) {
        this.order = order;
        this.family = family;
    }



    public int getOrder() {
        return order;
    }

    public ProcessorFamily getFamily() {
        return family;
    }

    public enum ProcessorFamily {
        PRE_PROCESSOR,
        THINK_PROCESSOR,
        POST_PROCESSOR
    }

}
