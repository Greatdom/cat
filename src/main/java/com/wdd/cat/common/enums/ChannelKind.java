package com.wdd.cat.common.enums;


/**
 * &#064program: cat
 * &#064description: 外部连接来源的枚举,用来点明消息的来源
 * &#064author: black-cat
 * &#064create: 2026-11-15 19:51
 **/

public enum ChannelKind {
    INTERNAL_DATE,       // 站内日程发出的消息
    INTERNAL_DESIRE,       // 站内欲望
    //站外
    EXTERNAL_QQ      ;       // QQ
    //TODO 还有没实现的,例如ESP32 WECHAT EXTERNAL_AGENT

    public static ChannelKind fromWire(String s){
        return null; //TODO
    }   // 大小写不敏感，未知返回 null
    public String wire(){
        return null;///TODO
    }                            // 传输用小写
}