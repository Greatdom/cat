package com.wdd.cat.common.cache;

import com.wdd.cat.message.child.InboundMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
     * 取一批：每个桶只看队头一条，凑够 limit 就停。
     * <p><b>只 peek 不 poll</b>——跳板 ack 之后才真正删掉。
     */
    public List<InboundMessage> peek(int limit) {
        List<InboundMessage> out = new ArrayList<>();
        for (Queue<InboundMessage> q : buckets.values()) {
            if (out.size() >= limit) {
                break;
            }
            InboundMessage m = q.peek();
            if (m != null) {
                out.add(m);
            }
        }
        return out;
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
