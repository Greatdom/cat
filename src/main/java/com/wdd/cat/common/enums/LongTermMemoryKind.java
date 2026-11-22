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


    /** 对外界的经验总结,例如一般的知识和概念,具体事件,或者某人的口头禅和习惯,某实体的交互方法 */
    EVENT,

    /** 对某事物的态度和情感,由情绪长期演化而来。 */
    ATTITUDE,

    /** 这种记忆特指对某个角色面具的描述,例如和这个角色面具的关系如何;
     * 而多个角色面具可以指向同一个实体Entity,也就是说能做到一个qq号和一个wechat号指代同一个人,
     * 怎么指向呢?这条记忆的角色面具指向LongTermMemoryEntry的entities哈希表字段的得分最高的那一对
     * */
    OBJECT

}