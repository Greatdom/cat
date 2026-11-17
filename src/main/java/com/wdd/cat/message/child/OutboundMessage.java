package com.wdd.cat.message.child;


import com.wdd.cat.message.Message;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 出站消息
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-17 11:59
 **/

public class OutboundMessage extends Message {
    private List<NormalMessage> messages;   // 大模型的回答
    private InboundMessage inboundMessage;  //原来的问题,用于回顾信息

    public void setMessages(List<NormalMessage> messages) {
        this.messages = messages;
    }

    public InboundMessage getInboundMessage() {
        return inboundMessage;
    }

    public void setInboundMessage(InboundMessage inboundMessage) {
        this.inboundMessage = inboundMessage;
    }

    // 增删查等链表操作
    public void add(NormalMessage msg) {
        messages.add(msg);
    }

    public List<NormalMessage> getMessages() {
        return messages;
    }
}