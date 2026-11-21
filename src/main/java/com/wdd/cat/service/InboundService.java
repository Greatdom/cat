package com.wdd.cat.service;

import com.wdd.cat.archive.Archive;
import com.wdd.cat.archive.ArchiveStore;
import com.wdd.cat.channel.ChannelPoller;
import com.wdd.cat.channel.ChannelRegistry;
import com.wdd.cat.channel.ConvertContext;
import com.wdd.cat.channel.InboundConvertor;
import com.wdd.cat.channel.PollContext;
import com.wdd.cat.common.utils.CatScheduler;
import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import com.wdd.cat.common.enums.ChannelKind;
import com.wdd.cat.exception.PollException;
import com.wdd.cat.message.ChatKey;
import com.wdd.cat.message.child.InboundMessage;
import com.wdd.cat.message.child.NormalMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public InboundService(ArchiveStore archiveStore, ChannelRegistry channelRegistry,
                          CatScheduler scheduler) {
        this.archiveStore = archiveStore;
        this.channelRegistry = channelRegistry;
        scheduler.schedule("inbound.handle", UnEditAbleDefaultConfig.TICK_MS, this::handle);
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
                archive.id(), link, Set.copyOf(chats), UnEditAbleDefaultConfig.POLL_LIMIT);
        ConvertContext convCtx = new ConvertContext(
                archive.id(), link.kind(), link.account(), accountKeys,
                archive.session() == null ? null : archive.session().sessionId());

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
     * <p>渠道需要的一切都在 link / pollCtx 里，这里不做渠道特判。
     */
    @SuppressWarnings("unchecked")
    private <T> List<NormalMessage> pollAndConvert(ChannelRegistry.InboundPair<?> pair,
                                                   Archive.Link link,
                                                   PollContext pollCtx,
                                                   ConvertContext convCtx) throws PollException {
        ChannelPoller<T> poller = (ChannelPoller<T>) pair.poller();
        InboundConvertor<T> convertor = (InboundConvertor<T>) pair.convertor();

        return convertor.convert( poller.poll(pollCtx), convCtx);
    }

    private static InboundMessage toInbound(NormalMessage src) {
        InboundMessage out = new InboundMessage();
        out.setArchiveId(src.getArchiveId());
        out.setChannel(src.getChannel());
        out.setSource(src.getSource());
        out.setTimestamp(src.getTimestamp());
        out.setChannelMessageId(src.getChannelMessageId());
        out.setSessionId(src.getSessionId());
        out.setRaw(src.getRaw());
        out.setContent(src.getContent());
        return out;
    }
}
