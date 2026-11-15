package com.wdd.cat.archive;


import java.util.List;

/**
 * &#064program: cat
 * <p>
 * &#064description: description
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 20:35
 **/

/**
 * 一个存档 = 一个模拟人。
 * 包含身份（名字/开关）、记忆库、DSH 会话、外部连接、认识的人、
 * 反射规则、装配单、时间戳。
 */
public record Archive(
        String version,                   //TODO 存档版本,方便版本迁移,初始化时注入UnEditAbleDefaultConfig的ARCHIVE_VERSION
        long id,                          // 雪花 ID，传给跳板时转 String
        String name,                      // 存档名
        Boolean enabled,                  // 是否启用（停用后不跑任何轮次）
        Memory memory,                    // 记忆地址  //TODO
        Session session,                  // 绑定的会话
        List<Link> links,                 // 外部连接（QQ / 微信 / ESP32）
//        List<Contact> contacts,           // 这个存档认识谁
       //TODO List<ReflexRule> reflexes,        // 反射规则表（不思考直接回应）
        PipelineConfig pipeline,          // 装配单：每个流程钩子用哪个实现
        String createdAt,                 // 创建时间
        String updatedAt                  // 更新时间
) {
    public boolean isEnabled(){
        return enabled;//TODO
    }           // enabled 为空视为 false，避免 NPE

    /** 会话状态：BOUND 已绑定 / LOST 掉了，需要重绑。 */
    public enum SessionStatus { BOUND, LOST }

    /** 记忆库引用。 */
    public record Memory(String storeId) {
        //TODO
        }

    /** 绑定的  会话。 */
    public record Session(
            String sessionId,             // 会话的ID
            SessionStatus status,         // 绑定状态
            //TODO 再加个字段:这个字段描述这个会话是在哪个跳板的,例如是自研agent的还是dsh的
            String title                  // 会话的名字
    ) {}

    /** 外部连接：这个存档能通过什么方式对外发声/接收。 */
    public record Link(
            Long id,
            String kind,      //TODO 应该用ChannelKind    "qq" / "wechat" / "esp32"
            Boolean enabled,
            String account,               // 账号（QQ 号 / 设备名）
            String baseUrl,               // 跳板地址
            String token,                 // 鉴权 token
            String deviceId               // 硬件设备 ID（esp32 用）
    ) {
        public boolean isEnabled(){
            return enabled;//TODO
        }       // 同上，null 视为 false
    }

//    /** 这个存档认识谁。 */
//    public record Contact(
//            String chatType,              // "group" / "private"
//            String chatId,                // 会话 ID
//            Boolean enabled               // 是否允许联系
//    ) {
//        public boolean isEnabled();
//    }
}