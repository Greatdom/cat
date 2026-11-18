package com.wdd.cat.message.child;

import com.wdd.cat.message.Content;
import com.wdd.cat.message.Message;

/**
 * &#064;program: cat
 * <p>
 * &#064;description: 基础消息
 * <p>
 * &#064;author: black-cat
 * <p>
 * &#064;create: 2026-11-15 21:23
 **/
public class NormalMessage extends Message {

    private Object raw;                 // 原始报文，调试用，平时可以丢
    private Content content;            // 消息内容（文本/文件/坐标/多段）

    /** 全部文本，给拼 Prompt 用。 */
    public String text() {
        return Content.plainText(content);
    }

    public Object getRaw() {
        return raw;
    }

    public void setRaw(Object raw) {
        this.raw = raw;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

//TODO 重写string,equals和hashcode方法
}