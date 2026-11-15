package com.wdd.cat.archive;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * &#064program: cat
 * <p>
 * &#064description: 存档仓库：存档的增删改查 + 账号索引
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 20:54
 **/

//五条校验：
//
//存档总数 ≤ cat.archive.max-count（默认 4）
//同渠道账号唯一
//会话唯一
//名字唯一
//群聊白名单可共享（打日志「允许抢话」）

//TODO 第一版可以先放内存 Map，之后换文件/数据库。
@Component
public class ArchiveStore {
    /** 全部存档。 */
    public List<Archive> all() {
        // TODO 返回所有存档
        return List.of();
    }

    /** 按 ID 找存档。 */
    public Optional<Archive> find(long id) {
        // TODO 按 id 查
        return Optional.empty();
    }

    /** 新建存档。 */
    public Archive create(Archive draft) {
        // TODO 分配雪花 id、补 createdAt/updatedAt、存进去
        // TODO 超过 maxCount() 时拒绝
        return null;
    }

    /** 更新存档。 */
    public Archive update(long id, Archive draft) {
        // TODO 先 find，找不到抛异常；找到则保留 id，刷新 updatedAt
        return null;
    }

    /** 删除存档。 */
    public void delete(long id) {
        // TODO 从仓库移除，同时清掉它的账号索引
    }

    /** 改会话绑定状态（BOUND / LOST）。 */
    public void markSession(long id, Archive.SessionStatus status) {
        // TODO 找到存档，替换 session.status，刷新 updatedAt
    }

    /** 所有已占用的账号，格式 "qq:10000"。新建 Link 时用它查重。 */
    public Set<String> allAccounts() {
        // TODO 遍历所有存档的所有 link，拼成 "kind:account"
        return Set.of();
    }

    /** 只返回启用的存档，跑轮次时用。 */
    public List<Archive> enabled() {
        // TODO filter(Archive::isEnabled)
        return List.of();
    }

    /** 存档上限，默认 4。 */
    public int maxCount() {
        // TODO 从配置读，没有则返回 4
        return 4;
    }
}