package com.wdd.cat.processor;


import com.wdd.cat.message.InboundMessage;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 处理器接口
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 12:12
 **/

public interface Processor {
    String kind();
    int order();                    // 执行顺序,小的先执行
    List<InboundMessage> process(List<InboundMessage> in); //处理器核心方法
}
