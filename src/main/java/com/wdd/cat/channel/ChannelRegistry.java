package com.wdd.cat.channel;


import com.wdd.cat.common.enums.ChannelKind;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
/**
 * &#064program: cat
 * <p>
 * &#064description: 通道注册表：按渠道类型查出「拉/转」或「转/发」这一对实现,每一对都是要匹配的,这样触发篡改消息源,否则不会乱发消息
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:47
 **/


@Component
public class ChannelRegistry {

    /** 入站一对：poller 负责拉取，convertor 负责把拉到的原始报文转成 InboundMessage。 */
    public Optional<InboundPair<?>> inboundPair(ChannelKind kind){
        return Optional.empty();//TODO
    }

    /** 出站一对：convertor 负责把 OutboundMessage 转成载荷，sender 负责把载荷发出去。 */
    public Optional<OutboundPair<?>> outboundPair(ChannelKind kind){
        return Optional.empty();//TODO
    }

    /** 当前有哪些渠道可用（只填了 poller 的算入站，只填了 sender 的算出站）。 */
    public Set<ChannelKind> available(){
        return null;//TODO
    }

    public record InboundPair<T>(ChannelPoller<T> poller, InboundConvertor<T> convertor) {}
    public record OutboundPair<P>(OutboundConvertor<P> convertor, ChannelSender<P> sender) {}
//
//    public record InboundPair(ChannelPoller<?> poller, InboundConvertor<?> convertor) {}
//    public record OutboundPair(OutboundConvertor<?> convertor, ChannelSender<?> sender) {}
}