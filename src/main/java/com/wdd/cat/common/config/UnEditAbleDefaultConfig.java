package com.wdd.cat.common.config;

/**
 * &#064program: cat
 * &#064description: 不可配置项，只有开发者能改源码修改
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

    /**
     * CatScheduler线程池的池子上限
     */
    public static final int CAT_SCHEDULER_POOL_LIMIT = 10;

    private UnEditAbleDefaultConfig() {
    }
}
