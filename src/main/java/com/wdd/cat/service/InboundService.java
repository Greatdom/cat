package com.wdd.cat.service;

import com.wdd.cat.archive.Archive;
import com.wdd.cat.archive.ArchiveStore;
import com.wdd.cat.channel.ChannelRegistry;
import com.wdd.cat.channel.ConvertContext;
import com.wdd.cat.channel.PollContext;
import com.wdd.cat.channel.onebot.OneBotClient;
import com.wdd.cat.channel.onebot.OneBotConnectionPool;
import com.wdd.cat.channel.onebot.OneBotPoller;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.message.ChatKey;
import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.message.child.NormalMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * &#064program: cat
 * &#064description: 轮询外部消息并写入缓存池
 * &#064author: black-cat
 * &#064create: 2026-11-15 22:42
 **/
@Component
public class InboundService {

    private static final Logger log = LoggerFactory.getLogger(InboundService.class);

    /** 每次拉多少条历史。 */
    private static final int POLL_LIMIT = 20;

    /** 缓存池：InboundService 写，PreService 读。 */
    private final ConcurrentLinkedQueue<InboundMessage> inboundMessageCache = new ConcurrentLinkedQueue<>();

    private final ArchiveStore archiveStore;
    private final ChannelRegistry channelRegistry;
    private final OneBotConnectionPool oneBotPool;
    private final OneBotPoller oneBotPoller;

    public InboundService(ArchiveStore archiveStore,
                          ChannelRegistry channelRegistry,
                          OneBotConnectionPool oneBotPool,
                          OneBotPoller oneBotPoller) {
        this.archiveStore = archiveStore;
        this.channelRegistry = channelRegistry;
        this.oneBotPool = oneBotPool;
        this.oneBotPoller = oneBotPoller;
    }

    /** 从缓存池取出全部（给 PreService 用）。 */
    public void drainTo(List<InboundMessage> target) {
        InboundMessage msg;
        while ((msg = inboundMessageCache.poll()) != null) {
            target.add(msg);
        }
    }

    /** 当前缓存条数，排障用。 */
    public int cacheSize() {
        return inboundMessageCache.size();
    }

    @Scheduled(fixedDelayString = "${cat.tick.ms:500}")
    void handle() {
        Set<String> accountKeys = archiveStore.allAccounts();

        for (Archive archive : archiveStore.enabled()) {
            if (archive.links() == null) {
                continue;
            }
            for (Archive.Link link : archive.links()) {
                if (!link.isEnabled()) {
                    continue;
                }
                if (link.kind().getFamily() != ChannelKind.ChannelFamily.EXTERNAL) {
                    continue;
                }
                try {
                    pollOne(archive, link, accountKeys);
                } catch (Exception e) {
                    // 一个存档出事不拖垮别的
                    log.warn("存档 {} 的 {} 拉取失败：{}", archive.id(), link.kind(), e.getMessage());
                }
            }
        }
    }

    private void pollOne(Archive archive, Archive.Link link, Set<String> accountKeys) throws Exception {
        List<ChatKey> chats = archive.contactsOf(link.kind()).stream()
                .map(Archive.Contact::chatKey)
                .collect(Collectors.toList());
        if (chats.isEmpty()) {
            return;
        }

        PollContext pollCtx = new PollContext(
                archive.id(), link.account(), Set.copyOf(chats), POLL_LIMIT);
        ConvertContext convCtx = new ConvertContext(
                archive.id(), link.kind(), link.account(), accountKeys);

        List<NormalMessage> normal = pollRaw(link, pollCtx, convCtx);
        if (normal.isEmpty()) {
            return;
        }

        for (NormalMessage m : normal) {
            inboundMessageCache.add(toInbound(m));
        }
        log.info("存档 {} 拉到 {} 条（缓存 {}）", archive.id(), normal.size(), inboundMessageCache.size());
    }

    private List<NormalMessage> pollRaw(Archive.Link link, PollContext pollCtx, ConvertContext convCtx)
            throws Exception {
        ChannelRegistry.InboundPair<?> pair = channelRegistry.inboundPair(link.kind()).orElse(null);
        if (pair == null) {
            return List.of();
        }

        if (link.kind() == ChannelKind.EXTERNAL_QQ) {
            OneBotClient client = oneBotPool.forAccount(pollCtx.archiveId(), link);
            List<JsonNode> raw = oneBotPoller.poll(client, pollCtx);
            // 泛型擦除，形状由同一个 kind 保证
            @SuppressWarnings("unchecked")
            var convertor = (com.wdd.cat.channel.InboundConvertor<JsonNode>) pair.convertor();
            return convertor.convert(raw, convCtx);
        }
        return List.of();
    }

    private static InboundMessage toInbound(NormalMessage src) {
        InboundMessage out = new InboundMessage();
        out.setArchiveId(src.getArchiveId());
        out.setChannel(src.getChannel());
        out.setSource(src.getSource());
        out.setTimestamp(src.getTimestamp());
        out.setChannelMessageId(src.getChannelMessageId());
        out.setRaw(src.getRaw());
        out.setContent(src.getContent());
        return out;
    }
}
