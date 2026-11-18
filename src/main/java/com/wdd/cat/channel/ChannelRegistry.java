package com.wdd.cat.channel;

import com.wdd.cat.common.enums.ChannelKind;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * &#064program: cat
 * &#064description: 通道注册表：按渠道类型查出「拉/转」或「转/发」这一对实现
 * &#064author: black-cat
 * &#064create: 2026-11-15 21:47
 **/
@Component
@SuppressWarnings({"rawtypes", "unchecked"})
public class ChannelRegistry {

    private static final Logger log = LoggerFactory.getLogger(ChannelRegistry.class);

    private final Map<ChannelKind, InboundPair<?>> inbound = new EnumMap<>(ChannelKind.class);
    private final Map<ChannelKind, OutboundPair<?>> outbound = new EnumMap<>(ChannelKind.class);

    private final List<ChannelPoller<?>> pollers;
    private final List<InboundConvertor<?>> inboundConvertors;
    private final List<OutboundConvertor<?>> outboundConvertors;
    private final List<ChannelSender<?>> senders;

    public ChannelRegistry(List<ChannelPoller<?>> pollers,
                           List<InboundConvertor<?>> inboundConvertors,
                           List<OutboundConvertor<?>> outboundConvertors,
                           List<ChannelSender<?>> senders) {
        this.pollers = pollers;
        this.inboundConvertors = inboundConvertors;
        this.outboundConvertors = outboundConvertors;
        this.senders = senders;
    }

    @PostConstruct
    void build() {
        for (ChannelPoller<?> poller : pollers) {
            ChannelKind kind = poller.kind();
            if (kind == null) {
                log.error("{} 的 kind() 返回 null，跳过", poller.getClass().getSimpleName());
                continue;
            }
            InboundConvertor<?> convertor = findInbound(kind);
            if (convertor == null) {
                log.warn("渠道 {} 有 poller 但缺 InboundConvertor，入站不可用", kind);
                continue;
            }
            inbound.put(kind, new InboundPair(poller, convertor));
        }

        for (OutboundConvertor<?> convertor : outboundConvertors) {
            ChannelKind kind = convertor.kind();
            if (kind == null) {
                log.error("{} 的 kind() 返回 null，跳过", convertor.getClass().getSimpleName());
                continue;
            }
            ChannelSender<?> sender = findSender(kind);
            if (sender == null) {
                log.warn("渠道 {} 有 OutboundConvertor 但缺 ChannelSender，出站不可用", kind);
                continue;
            }
            outbound.put(kind, new OutboundPair(convertor, sender));
        }

        log.info("渠道注册完成：入站 {} 个，出站 {} 个", inbound.size(), outbound.size());
    }

    private InboundConvertor<?> findInbound(ChannelKind kind) {
        return inboundConvertors.stream()
                .filter(c -> c.kind() == kind)
                .findFirst()
                .orElse(null);
    }

    private ChannelSender<?> findSender(ChannelKind kind) {
        return senders.stream()
                .filter(s -> s.kind() == kind)
                .findFirst()
                .orElse(null);
    }

    /** 入站一对。 */
    public Optional<InboundPair<?>> inboundPair(ChannelKind kind) {
        return Optional.ofNullable(inbound.get(kind));
    }

    /** 出站一对。 */
    public Optional<OutboundPair<?>> outboundPair(ChannelKind kind) {
        return Optional.ofNullable(outbound.get(kind));
    }

    /** 当前可用的渠道（入站或出站任一可用即算）。 */
    public Set<ChannelKind> available() {
        Set<ChannelKind> out = EnumSet.noneOf(ChannelKind.class);
        out.addAll(inbound.keySet());
        out.addAll(outbound.keySet());
        return out;
    }

    /** 入站和出站配对类型：poller 与 convertor 必须同 T。 */
    public record InboundPair<T>(ChannelPoller<T> poller, InboundConvertor<T> convertor) {}

    /** 出站和入站配对类型：convertor 与 sender 必须同 P。 */
    public record OutboundPair<P>(OutboundConvertor<P> convertor, ChannelSender<P> sender) {}
}
