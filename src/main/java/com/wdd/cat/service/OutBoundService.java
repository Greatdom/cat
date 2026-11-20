package com.wdd.cat.service;

import com.wdd.cat.archive.Archive;
import com.wdd.cat.archive.ArchiveStore;
import com.wdd.cat.channel.ChannelRegistry;
import com.wdd.cat.channel.ChannelSender;
import com.wdd.cat.channel.OutboundConvertor;
import com.wdd.cat.channel.SendContext;
import com.wdd.cat.common.config.EditAbleDefaultConfig;
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
 * &#064description: 出站业务：拆 OutboundMessage、按存档分桶排队、按频率交给 sender
 * <p>
 * 和 InboundService 一个形状：只认 archiveStore + channelRegistry，
 * 渠道实现从注册表查，不依赖任何具体 sender。
 * <p>
 * 三处限流的第三处就在这里：每桶每次只发一条，节奏是
 * {@link EditAbleDefaultConfig#qqPollAndSendIntervalMs}。
 * <p>
 * 本版发送失败直接丢弃 —— 防丢失留到以后做。
 * &#064author: black-cat
 * &#064create: 2026-11-15 22:42
 **/
@Component
public class OutBoundService {

    private static final Logger log = LoggerFactory.getLogger(OutBoundService.class);

    /** 待发缓存：PostService 写，本类读。键 = 存档 id，一个存档一个桶。 */
    private final Map<Long, Queue<NormalMessage>> sendQueues = new ConcurrentHashMap<>();

    /** 每个桶上次发送时间，用于控频。 */
    private final Map<Long, Long> lastSentAt = new ConcurrentHashMap<>();

    private final ArchiveStore archiveStore;
    private final ChannelRegistry channelRegistry;

    public OutBoundService(ArchiveStore archiveStore, ChannelRegistry channelRegistry,
                           CatScheduler scheduler) {
        this.archiveStore = archiveStore;
        this.channelRegistry = channelRegistry;
        scheduler.schedule("outbound.handle", UnEditAbleDefaultConfig.TICK_MS, this::handle);
    }

    /** 写进缓存池：拆成 NormalMessage，按存档分桶（给 PostService 用）。 */
    public void enqueue(OutboundMessage out) {
        if (out == null || out.getMessages() == null || out.getMessages().isEmpty()) {
            return;
        }
        Queue<NormalMessage> bucket = sendQueues.computeIfAbsent(
                out.getArchiveId(), k -> new ConcurrentLinkedQueue<>());
        for (NormalMessage msg : out.getMessages()) {
            if (msg == null) {
                continue;
            }
            msg.setArchiveId(out.getArchiveId());
            bucket.add(msg);
        }
        log.info("存档 {} 入出站队列 {} 条（待发 {}）",
                out.getArchiveId(), out.getMessages().size(), cacheSize());
    }

    /** 当前各桶积压总数，排障用。 */
    public int cacheSize() {
        return sendQueues.values().stream().mapToInt(Queue::size).sum();
    }

    /** 定时器：每个桶到点就发一条。 */
    void handle() {
        long now = System.currentTimeMillis();
        for (Map.Entry<Long, Queue<NormalMessage>> entry : sendQueues.entrySet()) {
            long archiveId = entry.getKey();
            if (!due(archiveId, now)) {
                continue;
            }
            NormalMessage msg = entry.getValue().peek();
            if (msg == null) {
                continue;
            }
            lastSentAt.put(archiveId, now);
            // 成不成功都出队：本版失败直接丢弃，防丢失留到以后
            entry.getValue().poll();
            sendOne(archiveId, msg);
        }
    }

    /** 这个桶到发送间隔了吗？只读，不改时间戳。 */
    private boolean due(long archiveId, long now) {
        Long last = lastSentAt.get(archiveId);
        long interval = EditAbleDefaultConfig.qqPollAndSendIntervalMs;
        return last == null || now - last >= interval;
    }

    /** 发一条：从存档找连接，从注册表找「转 + 发」那一对。 */
    private void sendOne(long archiveId, NormalMessage msg) {
        Archive archive = archiveStore.find(archiveId).orElse(null);
        if (archive == null) {
            log.warn("存档 {} 不存在，这条发不出去：{}", archiveId, msg.text());
            return;
        }
        ChannelKind channel = msg.getChannel();
        Archive.Link link = linkOf(archive, channel);
        if (link == null) {
            log.warn("存档 {} 没有 {} 渠道的连接，这条发不出去", archiveId, channel);
            return;
        }
        ChannelRegistry.OutboundPair<?> pair = channelRegistry.outboundPair(link.kind()).orElse(null);
        if (pair == null) {
            log.debug("渠道 {} 没有可用的出站实现，丢弃", link.kind());
            return;
        }

        SendContext ctx = new SendContext(archiveId, link, msg.chatType(), msg.chatId());
        if (sendThrough(pair, ctx, msg) == 0) {
            log.warn("存档 {} 发送失败，已丢弃：{}", archiveId, msg.text());
        }
    }

    /** 交给配对的 convertor + sender；这里不做渠道特判。 */
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

    /** 存档被删时清掉它的桶。 */
    public void forget(long archiveId) {
        sendQueues.remove(archiveId);
        lastSentAt.remove(archiveId);
    }
}
