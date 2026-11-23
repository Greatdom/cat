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


    /** 对外界的经验总结,例如一般的知识和概念,具体事件,或者某人的口头禅和习惯,某实体的交互方法(客观事实) */
    EVENT,

    /** 对某事物的态度和情感,由情绪长期演化而来(主观臆断) */
    ATTITUDE,


}