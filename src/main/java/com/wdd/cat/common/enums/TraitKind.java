package com.wdd.cat.common.enums;


/**
 * &#064program: cat
 * &#064description: agent的性格维度枚举,参照大五人格理论
 * &#064author: black-cat
 * &#064create: 2026-11-15 19:57
 **/

public enum TraitKind {
    OPENNESS,            // 开放性
    CONSCIENTIOUSNESS,   // 尽责性
    EXTRAVERSION,        // 外向性
    AGREEABLENESS,       // 宜人性
    NEUROTICISM;         // 神经质

    public String wire(){
        return null;//TODO
    }
    public static TraitKind fromWire(String s){
        return null;//TODO
    }
}
