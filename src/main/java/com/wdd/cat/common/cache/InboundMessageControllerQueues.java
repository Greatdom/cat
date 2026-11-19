package com.wdd.cat.common.cache;

import com.wdd.cat.message.ChatKey;
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
 * &#064description: 待投递队列：按「存档 + 会话」分桶，每次每桶只出一条
 * &#064author: black-cat
 * &#064create: 2026-11-19 11:10
 **/
@Component
public class InboundMessageControllerQueues {

    /** 键 = 存档 id + 会话；必须带存档，否则两个存档抢同一个群会互相抢消息。 */
    private final Map<String, Queue<InboundMessage>> buckets = new ConcurrentHashMap<>();

    private static String bucketKey(long archiveId, ChatKey chat) {
        return archiveId + "|" + chat.wire();
    }

    /** 入队。 */
    public void enqueue(InboundMessage msg) {
        ChatKey chat = msg.chatKey();
        if (chat == null) {
            return;
        }
        buckets.computeIfAbsent(bucketKey(msg.getArchiveId(), chat),
                        k -> new ConcurrentLinkedQueue<>())
                .add(msg);
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

    /** 存档删除时清空它的桶。 */
    public void forget(long archiveId) {
        String prefix = archiveId + "|";
        buckets.keySet().removeIf(k -> k.startsWith(prefix));
    }
}
