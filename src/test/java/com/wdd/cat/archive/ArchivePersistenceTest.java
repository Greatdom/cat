package com.wdd.cat.archive;

import com.wdd.cat.common.config.UnEditAbleDefaultConfig;
import com.wdd.cat.common.utils.JsonCodec;
import com.wdd.cat.common.utils.SnowflakeId;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 落盘往返测试：写数据 -> 新建一个 store -> 读回来 -> 一样。
 * <p>注意：这里用的是真实 data/ 目录下的存档，跑完自己清理。
 */
class ArchivePersistenceTest {

    @Test
    void archiveSurvivesRestart() throws Exception {
        SnowflakeId ids = new SnowflakeId();
        JsonCodec json = new JsonCodec();

        ArchiveStore first = new ArchiveStore(ids, json);
        String name = "持久化测试-" + System.nanoTime();
        Archive created = first.create(new Archive(
                "1", 0, name, true, null, null, List.of(),
                PipelineConfig.defaultConfig(), null, null));

        Path file = Path.of(UnEditAbleDefaultConfig.DATA_DIR)
                .resolve(Long.toString(created.id()))
                .resolve("archive.json");
        assertThat(file).exists();
        assertThat(Files.readString(file)).contains(name);

        try {
            // 模拟重启：新建 store，loadAll 会从磁盘读
            ArchiveStore second = new ArchiveStore(ids, json);
            second.loadAll();
            assertThat(second.find(created.id())).isPresent()
                    .get()
                    .extracting(Archive::name)
                    .isEqualTo(name);
        } finally {
            first.delete(created.id());
        }
    }
}
