package com.wdd.cat.processor.post_processor;


import com.wdd.cat.message.child.NormalMessage;
import com.wdd.cat.message.child.OutboundMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 消息润色,拆分
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 19:28
 **/
@Component
public class ExpressProcessor implements PostProcessor{
    //废弃E:\程序代码\dsh\references\cat-legacy\cat\src\main\java\com\wdd\cat\link\PromptComposer.java的业务
    @Override
    public String kind() {
        return "ExpressProcessor";
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public List<OutboundMessage> process(List<OutboundMessage> in) {
        return List.of();//消息润色,拆分
    }
}
