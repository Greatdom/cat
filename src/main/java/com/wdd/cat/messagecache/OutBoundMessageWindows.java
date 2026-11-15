package com.wdd.cat.messagecache;


import org.springframework.stereotype.Component;

/**
 * &#064program: cat
 * <p>
 * &#064description: 出站消息队列链表
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 23:26
 **/
@Component
public class OutBoundMessageWindows {
    //TODO根据OutboundService的实现,决定这个类要维护的是线程安全的还是非线程安全的队列List<Queue>;要维护多个队列;每个队列指代一个外部连接客户端
    //设置一定的队列长度上限,超限按策略执行某个动作(例如丢弃)

}
