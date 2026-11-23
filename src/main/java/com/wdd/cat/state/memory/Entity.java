package com.wdd.cat.state.memory;


import java.util.HashMap;

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
        String name,     //实体的取名
        EntityFamily entityFamily   //实体的原型

){
    public enum EntityFamily {
        MASTER,   //存档的主人
        SELF,   //自己
        OBJECT,  //物体
        FAMILIAR,  //认识的人
        STRANGER,   //陌生人
    }

}
