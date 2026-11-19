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
 * &#064description: 待投递队列：按 DSH 会话（sessionId）分桶，每次每桶只出一条
 * <p>
 * 跳板取走时每个桶各取上限一条；会话不存在的消息由跳板丢弃并记日志。
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

    /** 取一批：每个桶只取队头一条，凑够 limit 就停。 */
    public List<InboundMessage> take(int limit) {
        List<InboundMessage> out = new ArrayList<>();
        for (Queue<InboundMessage> q : buckets.values()) {
            if (out.size() >= limit) {
                break;
            }
            InboundMessage m = q.poll();
            if (m != null) {
                out.add(m);
            }
        }
        return out;
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
