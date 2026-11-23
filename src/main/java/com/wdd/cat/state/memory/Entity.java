package com.wdd.cat.state.memory;


import java.util.HashMap;
import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: 某个实体,例如某个熟人,某个陌生人,存档"我"自己;也可以是物体,实体归存档私有
 * 对于一个存档,分别必有且只有一个MASTER实体指代主人.SELF实体指代自己和STRANGER实体指代没有识别到的人
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-22 17:35
 **/

public record Entity(
        String id,  //实体唯一标识
        EntityFamily entityFamily,   //实体的原型
        List<Entity> entity
        //如果"我"意识到某N+1个实体其实是同一个实体,可以用内置链表把真实感不那么强的剩下N个个实体串联起来
        //如果"我"又意识到这N+1个指代同一个实体的实体中有其中的实体应该不是同一个实体,就剔除掉某个

){
    public enum EntityFamily {
        MASTER,   //存档的主人
        SELF,   //自己
        OBJECT,  //物体
        FAMILIAR,  //认识的人
        STRANGER,   //陌生人
    }

}
