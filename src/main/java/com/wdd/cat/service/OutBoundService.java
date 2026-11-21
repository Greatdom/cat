package com.wdd.cat.service;

import com.wdd.cat.archive.Archive;
import com.wdd.cat.archive.ArchiveStore;
import com.wdd.cat.channel.ChannelRegistry;
import com.wdd.cat.channel.ChannelSender;
import com.wdd.cat.channel.OutboundConvertor;
import com.wdd.cat.channel.SendContext;
import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.common.utils.CatScheduler;
import com.wdd.cat.message.child.NormalMessage;
import com.wdd.cat.message.child.OutboundMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * &#064program: cat
 * &#064description: 出站业务：按渠道分桶排队，按各自节奏调 sender 发出去
 * &#064author: black-cat
 * &#064create: 2026-11-15 22:42
 **/
@Component
public class OutBoundService {

    private static final Logger log = LoggerFactory.getLogger(OutBoundService.class);

    /** 待发缓存,按渠道分桶 */
    private final Map<ChannelKind, Queue<Pending>> sendQueues = new ConcurrentHashMap<>();

    /** 每个渠道上次发送时间 */
    private final Map<ChannelKind, Long> lastSentAt = new ConcurrentHashMap<>();

    private final ArchiveStore archiveStore;
    private final ChannelRegistry channelRegistry;

    public OutBoundService(ArchiveStore archiveStore, ChannelRegistry channelRegistry,
                           CatScheduler scheduler) {
        this.archiveStore = archiveStore;
        this.channelRegistry = channelRegistry;
        scheduler.schedule("outbound.handle", UnEditAbleDefaultConfig.TICK_MS, this::handle);
    }

    void handle() {
        long now = System.currentTimeMillis();
        for (Map.Entry<ChannelKind, Queue<Pending>> entry : sendQueues.entrySet()) {
            ChannelKind channel = entry.getKey();
            Queue<Pending> bucket = entry.getValue();
            if (!due(channel, now)) {
                continue;
            }
            Pending pending = bucket.peek();
            if (pending == null) {
                continue;
            }
            lastSentAt.put(channel, now);
            // 成不成功都出队：本版失败直接丢弃，防丢失留到以后
            bucket.poll();
            sendOne(channel, pending);
        }
    }

    private void sendOne(ChannelKind channel, Pending pending) {
        Archive archive = archiveStore.find(pending.archiveId()).orElse(null);
        if (archive == null) {
            log.warn("存档 {} 不存在，这条发不出去：{}", pending.archiveId(), pending.message().text());
            return;
        }
        Archive.Link link = linkOf(archive, channel);
        if (link == null) {
            log.warn("存档 {} 没有 {} 渠道的连接，这条发不出去", archive.id(), channel);
            return;
        }
        ChannelRegistry.OutboundPair<?> pair = channelRegistry.outboundPair(link.kind()).orElse(null);
        if (pair == null) {
            log.debug("渠道 {} 没有可用的出站实现，丢弃", link.kind());
            return;
        }

        SendContext ctx = new SendContext(
                archive.id(), link, pending.message().chatType(), pending.message().chatId());
        if (sendThrough(pair, ctx, pending.message()) == 0) {
            log.warn("存档 {} 发送失败，已丢弃：{}", archive.id(), pending.message().text());
        }
    }

    @SuppressWarnings("unchecked")
    private <P> int sendThrough(ChannelRegistry.OutboundPair<?> pair, SendContext ctx,
                                NormalMessage msg) {
        OutboundConvertor<P> convertor = (OutboundConvertor<P>) pair.convertor();
        ChannelSender<P> sender = (ChannelSender<P>) pair.sender();

        List<P> payloads = convertor.convert(msg);
        if (payloads == null || payloads.isEmpty()) {
            return 0;
        }
        return sender.send(ctx, payloads);
    }

    /**
     * 缓存入队
     */
    public void enqueue(OutboundMessage out) {
        if (out == null || out.getMessages() == null || out.getMessages().isEmpty()) {
            return;
        }
        int queued = 0;
        for (NormalMessage msg : out.getMessages()) {
            if (msg == null) {
                continue;
            }
            msg.setArchiveId(out.getArchiveId());
            ChannelKind channel = msg.getChannel();
            if (channel == null) {
                log.warn("存档 {} 的出站消息没有渠道，丢弃", out.getArchiveId());
                continue;
            }
            sendQueues.computeIfAbsent(channel, k -> new ConcurrentLinkedQueue<>())
                    .add(new Pending(out.getArchiveId(), msg));
            queued++;
        }
        if (queued > 0) {
            log.info("存档 {} 入出站队列 {} 条（待发 {}）",
                    out.getArchiveId(), queued, cacheSize());
        }
    }

    /** 渠道到发送间隔特判 */
    private boolean due(ChannelKind channel, long now) {
        Long last = lastSentAt.get(channel);
        if (last != null && now - last < intervalOf(channel)) {
            return false;
        }
        return true;
    }

    private long intervalOf(ChannelKind channel) {
        ChannelRegistry.OutboundPair<?> pair = channelRegistry.outboundPair(channel).orElse(null);
        if (pair == null) {
            return Long.MAX_VALUE;
        }
        return pair.sender().suggestedSendIntervalMs();
    }

    /** 当前各桶积压总数 */
    public int cacheSize() {
        return sendQueues.values().stream().mapToInt(Queue::size).sum();
    }

    /** 找这个存档在该渠道上的连接。 */
    private static Archive.Link linkOf(Archive archive, ChannelKind channel) {
        if (archive.links() == null) {
            return null;
        }
        return archive.links().stream()
                .filter(l -> l.isEnabled() && (channel == null || l.kind() == channel))
                .findFirst()
                .orElse(null);
    }

    /** 存档被删时清掉它的消息。 */
    public void forget(long archiveId) {
        for (Queue<Pending> bucket : sendQueues.values()) {
            bucket.removeIf(p -> p.archiveId() == archiveId);
        }
    }

    /** 一条待发的消息 + 它属于哪个存档。 */
    private record Pending(long archiveId, NormalMessage message) {}
}
