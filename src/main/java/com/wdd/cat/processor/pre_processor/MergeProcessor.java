package com.wdd.cat.processor.pre_processor;

import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import com.wdd.cat.common.enums.ProcessorKind;
import com.wdd.cat.message.ChatKey;
import com.wdd.cat.message.Content;
import com.wdd.cat.message.child.InboundMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * &#064program: cat
 * &#064description: 合并消息：窗口内、最多 N 条合成一条
 * <p>
 * 每条消息先在桶里躺够 MERGE_WINDOW_MS，再从队头按「相邻间隔不大于窗口」贪心合并。
 * &#064author: black-cat
 * &#064create: 2026-11-15 23:17
 **/
@Component
public class MergeProcessor implements PreProcessor {

    private static final Logger log = LoggerFactory.getLogger(MergeProcessor.class);

    /** 按会话分桶的待合并队列。 */
    private final Map<ChatKey, Deque<InboundMessage>> buckets = new ConcurrentHashMap<>();

    @Override
    public ProcessorKind kind() {
        return ProcessorKind.MERGE_PROCESSOR;
    }

    @Override
    public int order() {
        return ProcessorKind.MERGE_PROCESSOR.getOrder();
    }


    //TODO 以后考虑将UnderstandProcessor移动到DedupProcessor和MergeProcessor中间,先进行消息理解,再根据理解结果决定合并策略
    // 例如,硬件的即时反馈会判定为很急,所以要立即合并或者不合并;社交软件消息可以先放一放再合并
    @Override
    public List<InboundMessage> process(List<InboundMessage> in) {
        long now = System.currentTimeMillis();

        // ① 按时间戳顺序入桶
        if (in != null) {
            for (InboundMessage msg : in) {
                ChatKey chat = msg.chatKey();
                if (chat == null) {
                    continue;
                }
                Deque<InboundMessage> q =
                        buckets.computeIfAbsent(chat, k -> new ArrayDeque<>());
                synchronized (q) {
                    q.addLast(msg);
                    // 队列内保持时间戳升序
                    if (q.size() > 1) {
                        List<InboundMessage> sorted = new ArrayList<>(q);
                        sorted.sort(Comparator.comparingLong(InboundMessage::getTimestamp));
                        q.clear();
                        q.addAll(sorted);
                    }
                }
            }
        }

        // ② 从队头取「已躺够窗口」的消息合并
        List<InboundMessage> out = new ArrayList<>();
        for (Map.Entry<ChatKey, Deque<InboundMessage>> e : buckets.entrySet()) {
            Deque<InboundMessage> q = e.getValue();
            synchronized (q) {
                while (!q.isEmpty()) {
                    InboundMessage head = q.peekFirst();
                    long waited = now - head.getTimestamp();
                    if (waited < UnEditAbleDefaultConfig.MERGE_WINDOW_MS) {
                        log.debug("桶 {} 头部只等了 {}ms，继续等", e.getKey().wire(), waited);
                        break;   // 还没躺够，下一轮再取
                    }
                    out.add(drainOne(q, now));
                }
            }
        }
        return out;
    }

    /** 从队头取一组能合并的消息，合成一条。 */
    private InboundMessage drainOne(Deque<InboundMessage> q, long now) {
        InboundMessage first = q.pollFirst();
        List<InboundMessage> group = new ArrayList<>(UnEditAbleDefaultConfig.MERGE_MAX_COUNT);
        group.add(first);

        while (!q.isEmpty() && group.size() < UnEditAbleDefaultConfig.MERGE_MAX_COUNT) {
            InboundMessage next = q.peekFirst();
            long gap = next.getTimestamp() - group.get(group.size() - 1).getTimestamp();
            if (gap > UnEditAbleDefaultConfig.MERGE_WINDOW_MS) {
                break;   // 断档，下一组
            }
            group.add(q.pollFirst());
        }

        if (group.size() == 1) {
            return first;
        }
        InboundMessage merged = merge(group);
        log.debug("合并 {} 条 → 1 条（{}）", group.size(), merged.chatKey().wire());
        return merged;
    }

    /** 按最后一条的身份、时间、消息号，正文拼成 Multi。 */
    private InboundMessage merge(List<InboundMessage> group) {
        InboundMessage last = group.get(group.size() - 1);
        List<Content> parts = new ArrayList<>(group.size());
        for (InboundMessage m : group) {
            Content c = m.getContent();
            if (c != null) {
                parts.add(c);
            }
        }
        InboundMessage merged = new InboundMessage();
        merged.setArchiveId(last.getArchiveId());
        merged.setChannel(last.getChannel());
        merged.setSource(last.getSource());
        merged.setTimestamp(last.getTimestamp());
        merged.setChannelMessageId(last.getChannelMessageId());
        merged.setSessionId(last.getSessionId());
        merged.setRaw(last.getRaw());
        merged.setContent(parts.isEmpty() ? null
                : parts.size() == 1 ? parts.get(0) : new Content.Multi(parts));
        return merged;
    }
}
