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

    private UnEditAbleDefaultConfig() {
    }
}
