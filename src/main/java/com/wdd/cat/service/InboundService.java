package com.wdd.cat.service;

import com.wdd.cat.archive.Archive;
import com.wdd.cat.archive.ArchiveStore;
import com.wdd.cat.channel.ChannelPoller;
import com.wdd.cat.channel.ChannelRegistry;
import com.wdd.cat.channel.ConvertContext;
import com.wdd.cat.channel.InboundConvertor;
import com.wdd.cat.channel.PollContext;
import com.wdd.cat.channel.onebot.OneBotPoller;
import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.exception.PollException;
import com.wdd.cat.message.ChatKey;
import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.message.child.NormalMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * &#064program: cat
 * &#064description: 轮询外部消息并写入缓存池
 * &#064author: black-cat
 * &#064create: 2026-11-15 22:42
 **/
@Component
public class InboundService {

    private static final Logger log = LoggerFactory.getLogger(InboundService.class);

    /** 缓存池：本类写，PreService 读。 */
    private final ConcurrentLinkedQueue<InboundMessage> inboundMessageCache = new ConcurrentLinkedQueue<>();

    private final ArchiveStore archiveStore;
    private final ChannelRegistry channelRegistry;

    public InboundService(ArchiveStore archiveStore, ChannelRegistry channelRegistry) {
        this.archiveStore = archiveStore;
        this.channelRegistry = channelRegistry;
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

    private void pollOne(Archive archive, Archive.Link link, Set<String> accountKeys)
            throws PollException {
        List<ChatKey> chats = archive.contactsOf(link.kind());
        if (chats.isEmpty()) {
            return;
        }

        ChannelRegistry.InboundPair<?> pair = channelRegistry.inboundPair(link.kind()).orElse(null);
        if (pair == null) {
            log.debug("渠道 {} 没有可用的入站实现，跳过", link.kind());
            return;
        }

        PollContext pollCtx = new PollContext(
                archive.id(), link.account(), Set.copyOf(chats), UnEditAbleDefaultConfig.POLL_LIMIT);
        ConvertContext convCtx = new ConvertContext(
                archive.id(), link.kind(), link.account(), accountKeys);

        List<NormalMessage> normal = pollAndConvert(pair, link, pollCtx, convCtx);
        if (normal.isEmpty()) {
            return;
        }

        for (NormalMessage m : normal) {
            inboundMessageCache.add(toInbound(m));
        }
        log.info("存档 {} 拉到 {} 条（缓存 {}）", archive.id(), normal.size(), inboundMessageCache.size());
    }

    /**
     * 按渠道分发到对应 poller，再交给配对的 convertor。
     * <p>渠道自己决定要不要额外依赖（如 OneBot 用连接池），这里只做分发。
     */
    @SuppressWarnings("unchecked")
    private <T> List<NormalMessage> pollAndConvert(ChannelRegistry.InboundPair<?> pair,
                                                   Archive.Link link,
                                                   PollContext pollCtx,
                                                   ConvertContext convCtx) throws PollException {
        ChannelPoller<T> poller = (ChannelPoller<T>) pair.poller();
        InboundConvertor<T> convertor = (InboundConvertor<T>) pair.convertor();

        List<T> raw = switch (link.kind()) {
            case EXTERNAL_QQ -> poller instanceof OneBotPoller oneBot
                    ? (List<T>) oneBot.poll(link, pollCtx)
                    : poller.poll(pollCtx);
            // 站内渠道没有外部来源，不会进入这里
            case INTERNAL_DATE, INTERNAL_DESIRE, ADD_DATE -> List.of();
        };

        return convertor.convert(raw, convCtx);
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
