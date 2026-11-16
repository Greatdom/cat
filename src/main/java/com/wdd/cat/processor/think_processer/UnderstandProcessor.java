package com.wdd.cat.processor.think_processer;


import com.wdd.cat.message.Message;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 消息理解思考处理器
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 12:51
 **/
@Component
public class UnderstandProcessor implements ThinkProcessor{
    @Override
    public String kind() {
        return "UnderstandProcessor";
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public List<Message> process(List<Message> in) {
        //理解：这条消息是什么（急不急、重不重要、对我好坏）
        return List.of();
    }
}
