package com.wdd.cat.common.enums;


/**
 * &#064program: cat
 * <p>
 * &#064description: Content 的类型标记，用于判断该怎么处理消息
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 20:33
 **/

public enum ContentKind {
    TEXT,         // 纯文本
    FILE,         // 文件引用（图片/语音/视频/文档）
    COORDINATE,   // 坐标（硬件上报位置）
    MULTI         // 多段组合
}