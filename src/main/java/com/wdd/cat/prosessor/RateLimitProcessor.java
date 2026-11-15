package com.wdd.cat.prosessor;

import com.wdd.cat.message.InboundMessage;
import com.wdd.cat.messagecache.InboundMessageWindows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 投递限流前处理器：超过频率将消息暂存并阻塞前处理器
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 23:24
 **/


/**
 * [300] 投递限流：超过频率就先压着，这一轮不投，下一轮轮询再试。
 * ESP32 不需要——本机硬件，不存在风控。
 */
@Component
public class RateLimitProcessor implements PreProcessor {

    @Autowired
    private InboundMessageWindows windows;

    @Override
    public String kind() {
        return "ratelimit";
    }

    @Override
    public int order() {
        return 300;
    }

    @Override
    public List<InboundMessage> process(List<InboundMessage> in) {
        // TODO ChannelKind.ESP32 时原样返回，不过限流

        // TODO 算 key = 某个渠道的某个会话

        // TODO 按key维护"是否超限"的状态变量并按是否超限决定消息放行或者将消息按照key存放进windows的queue

        // TODO in为空时返回 List.of()

        return in;
    }

    @Override
    public List<InboundMessage> outQueue() {
        return null;//TODO    让入站滑动窗口吐出所有队列

    }
}