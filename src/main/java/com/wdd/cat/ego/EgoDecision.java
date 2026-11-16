package com.wdd.cat.ego;


/**
 * &#064program: cat
 * <p>
 * &#064description: description
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 17:59
 **/

import com.wdd.cat.message.Message;

import java.util.Map;

/**
 * 三我吵完的结果。
 */
public record EgoDecision(
        Message message,         // 最终要说的/做的
        String reconciliation,     // 必须：一句人话解释为什么这么选
        Map<String, Object> trace  // 调试用：本我多少、超我多少、谁赢了
) {
    /** 决定不说话。 */
    public static EgoDecision silence(String why) {
        // TODO new EgoDecision(Response.silence(), why, Map.of())
        return null;
    }

    /** 决定回应。 */
    public static EgoDecision respond(Message m, String why) {
        // TODO new EgoDecision(m, why, Map.of())
        return null;
    }
}