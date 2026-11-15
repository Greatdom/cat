package com.wdd.cat.prosessor;

import com.wdd.cat.message.InboundMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 合并消息：T 秒窗口内、最多 N 条，会合并成一条
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 23:17
 **/


@Component
public class MergeProcessor implements PreProcessor {

    /** 攒批窗口，毫秒。 */
//    private static final long WINDOW_MS = T;

    /** 一次最多合并几条，超过就先合。 */
//    private static final int MAX_MERGE = N;

    @Override
    public String kind() {
        return "merge";
    }

    @Override
    public int order() {
        return 200;
    }

    @Override
    public List<InboundMessage> process(List<InboundMessage> in) {
        // TODO  ChannelKind.ESP32 时原样返回，不合并
        // TODO 用算法将消息链表进行合并成一条并重进链表,规则:
        // TODO 能合并的按时间戳顺序排序的至多N条消息,相邻消息的时间间隔不大于T
        //TODO 将合并后的消息链表返回
        return in;
    }
}