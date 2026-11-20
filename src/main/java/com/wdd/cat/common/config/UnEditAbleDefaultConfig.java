package com.wdd.cat.common.config;

/**
 * &#064program: cat
 * &#064description: 不可配置项，只有开发者能改源码修改
 * <p>
 * 合并窗口和条数在 {@link EditAbleDefaultConfig} 里 —— 那两个是设置页能调的。
 * 依赖它们的派生值这里用方法算，不用常量：常量在类加载时就定死，
 * 页面改完窗口它不会跟着变。
 * &#064author: black-cat
 * &#064create: 2026-11-15 19:47
 **/
public class UnEditAbleDefaultConfig {

    /** 存档文件格式版本，字段变了靠它做迁移。 */
    public static final String ARCHIVE_VERSION = "1";

    /** 存档数据根目录。 */
    public static final String DATA_DIR = "data";

    /** 各 Service 的 @Scheduled 周期（毫秒）。 */
    public static final long TICK_MS = 500;

    /** 日程检查周期（毫秒）。 */
    public static final long SCHEDULE_DATE_MS = 20_000;

    /** 欲望检查周期（毫秒）。 */
    public static final long SCHEDULE_DESIRE_MS = 1_800_000;

    /** 一次拉多少条历史。 */
    public static final int POLL_LIMIT = 20;

    /** CatScheduler 线程池的池子上限。 */
    public static final int CAT_SCHEDULER_POOL_LIMIT = 10;

    /** 消息标记为「处理中」后，比窗口多留多久才视为残留、放回重投（毫秒）。 */
    public static final long PROCESSING_TIMEOUT_MARGIN_MS = 10_000L;

    /**
     * 消息标记为「处理中」后多久视为残留、放回重投（毫秒）。
     * <p>合并窗口可配，所以这里每次现算，不缓存。
     */
    public static long processingTimeoutMs() {
        return EditAbleDefaultConfig.mergeWindowMs + PROCESSING_TIMEOUT_MARGIN_MS;
    }


    private UnEditAbleDefaultConfig() {
    }
}
