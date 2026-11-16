package com.wdd.cat.prosessor.preprocessor;


import com.wdd.cat.message.InboundMessage;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 前处理器接口,用来预处理信息;根据消息渠道的不同,执行不同的前处理策略:
 * 例如esp这样的硬件外部连接得到的信息执行MergeProcessor.process时策略是跳过
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 22:54
 **/

public interface PreProcessor {
    String kind();
    int order();                    // 执行顺序,小的先执行
    List<InboundMessage> process(List<InboundMessage> in); //处理器核心方法
}