package com.wdd.cat.common.cache;

import com.wdd.cat.message.child.InboundMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * &#064program: cat
 * &#064description: 待投递队列：按 DSH 会话（sessionId）分桶
 * <p>
 * 跳板取走时每个桶只取队头一条，且只 peek 不删——等跳板 ack 才真删。
 * 没被 ack 的消息留在原地，靠 DeliveryState 的超时机制兜底。
 * &#064author: black-cat
 * &#064create: 2026-11-19 11:10
 **/
@Component
public class InboundMessageControllerQueues {

    /** 键 = sessionId；一个 DSH 会话一个桶。 */
    private final Map<String, Queue<InboundMessage>> buckets = new ConcurrentHashMap<>();

    /** 入队。sessionId 已由 DedupProcessor 保证非空。 */
    public void enqueue(InboundMessage msg) {
        String sessionId = msg.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        buckets.computeIfAbsent(sessionId, k -> new ConcurrentLinkedQueue<>()).add(msg);
    }

    /** 入队一批。 */
    public void enqueueAll(List<InboundMessage> messages) {
        if (messages == null) {
            return;
        }
        messages.forEach(this::enqueue);
    }

    /**
     * 按给定的会话列表取一批：每个会话只取队头一条。
     * <p><b>只 peek 不 poll</b>——跳板 ack 之后才真正删掉。
     *
     * @param sessionIds 跳板这次能处理的会话；空表示什么都不取
     */
    public List<InboundMessage> peek(List<String> sessionIds) {
        List<InboundMessage> out = new ArrayList<>();
        if (sessionIds == null || sessionIds.isEmpty()) {
            return out;
        }
        for (String sessionId : sessionIds) {
            if (sessionId == null || sessionId.isBlank()) {
                continue;
            }
            Queue<InboundMessage> q = buckets.get(sessionId);
            if (q == null) {
                continue;
            }
            InboundMessage m = q.peek();
            if (m != null) {
                out.add(m);
            }
        }
        return out;
    }

    /** 某个会话当前有没有积压，排障用。 */
    public boolean hasPending(String sessionId) {
        Queue<InboundMessage> q = sessionId == null ? null : buckets.get(sessionId);
        return q != null && !q.isEmpty();
    }

    /** ack：从对应桶里删掉这条。 */
    public boolean ack(String sessionId, String channelMessageId) {
        if (sessionId == null || channelMessageId == null) {
            return false;
        }
        Queue<InboundMessage> q = buckets.get(sessionId);
        if (q == null) {
            return false;
        }
        return q.removeIf(m -> channelMessageId.equals(m.getChannelMessageId()));
    }


    /** 当前积压总数。 */
    public int size() {
        return buckets.values().stream().mapToInt(Queue::size).sum();
    }

    /** 清空某个会话的桶。 */
    public void forget(String sessionId) {
        if (sessionId != null) {
            buckets.remove(sessionId);
        }
    }
}
