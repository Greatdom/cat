package com.wdd.cat.common.enums;


/**
 * &#064program: cat
 * <p>
 * &#064description: 文件引用的细分类型，决定下游怎么处理该文件
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 20:34
 **/

public enum FileKind {
    IMAGE,   // 图片
    AUDIO,   // 音频（语音/音乐）
    VIDEO,   // 视频
    FILE     // 其他文件（文档等）
}