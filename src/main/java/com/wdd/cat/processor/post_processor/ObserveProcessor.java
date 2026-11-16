package com.wdd.cat.processor.post_processor;


import com.wdd.cat.message.Message;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 消息回顾
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:29
 **/

public class ObserveProcessor implements PostProcessor{
    @Override
    public String kind() {
        return "ObserveProcessor";
    }

    @Override
    public int order() {
        return 200;
    }

    @Override
    public List<Message> process(List<Message> in) {
        return List.of();//消息回顾
    }
}
