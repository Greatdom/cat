package com.wdd.cat.common.enums;


import java.util.List;

/**
 * &#064program: cat
 * &#064description: agent的需求种类枚举,参照马斯洛需求理论
 * &#064author: black-cat
 * &#064create: 2026-11-15 19:56
 **/

public enum NeedKind {
    // 生理层（第一版做：ENERGY / COMFORT）
    ENERGY, COMFORT, WARMTH, HEALTH,
    // 安全层
    CERTAINTY, CONTROL,
    // 归属层（第一版做：SOCIAL）
    SOCIAL, BE_NEEDED, INTIMACY,
    // 尊重层
    RECOGNITION, AUTONOMY,
    // 自我实现层（第一版做：CURIOSITY / ORDER）
    CURIOSITY, EXPRESSION, ORDER;

    /** 第一版启用的 */
    public static List<NeedKind> v1() {
        return List.of(SOCIAL, CURIOSITY, ORDER, ENERGY, COMFORT);
        //TODO
    }

    public String wire(){
        return null;//TODO
    }
    public static NeedKind fromWire(String s){
        return null;//TODO
    }
}
