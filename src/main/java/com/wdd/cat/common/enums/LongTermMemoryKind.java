package com.wdd.cat.common.enums;


/**
 * &#064program: cat
 * <p>
 * &#064description: 一条长时记忆属于哪一类.不同类别在检索、衰减、注入三处的处理都不一样：事实过期慢，事件会淡忘，偏好直接决定行为
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 20:15
 **/


public enum LongTermMemoryKind {
    //TODO MemoryKind的枚举项归纳的似乎不好
    /** 稳定的事实："她身高一米七"。衰减最慢。 */
    FACT,

    /** 某个时刻发生的事："上周她说想辞职"。会随时间淡忘。 */
    EVENT,

    /** 对某人的长期态度（情感基调）："和她说话让我安心"。由情绪长期演化而来。 */
    EMOTION,

    /** 这个存档认识谁："她是主人"、"这个人只是群里的路人"。 */
    RELATION,

    /** 对方,不只是主人,而且还有其他人的偏好："她喜欢被提醒"。检索时优先取，用于判断该不该做某事。 */
    PREFERENCE,

    /** 习惯和流程："她喜欢先听结论"。 */
    PROCEDURE
}