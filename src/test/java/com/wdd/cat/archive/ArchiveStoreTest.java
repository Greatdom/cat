package com.wdd.cat.archive;

import com.wdd.cat.common.utils.JsonCodec;
import com.wdd.cat.common.utils.SnowflakeId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchiveStoreTest {

    /** 记下测试建的存档，跑完自己删。 */
    private final List<Long> created = new ArrayList<>();

    private ArchiveStore store;

    @BeforeEach
    void setUp() {
        store = new ArchiveStore(new SnowflakeId(), new JsonCodec());
    }

    @AfterEach
    void tearDown() {
        created.forEach(store::delete);
    }

    private Archive draft(String name, String account) {
        return new Archive(
                "1", 0, name, true, null, null,
                List.of(new Archive.Link(1L, "EXTERNAL_QQ", true, account,
                        "http://127.0.0.1:3000", "tok", null)),
                PipelineConfig.defaultConfig(), null, null
        );
    }

    private Archive create(Archive d) {
        Archive a = store.create(d);
        created.add(a.id());
        return a;
    }

    @Test
    void createAssignsIdAndTimestamps() {
        Archive a = create(draft("黑猫", "10000"));
        assertThat(a.id()).isPositive();
        assertThat(a.version()).isEqualTo("1");
        assertThat(a.createdAt()).isNotNull();
        assertThat(a.name()).isEqualTo("黑猫");
    }

    @Test
    void findAfterCreate() {
        Archive a = create(draft("黑猫", "10000"));
        assertThat(store.find(a.id())).isPresent();
        assertThat(store.find(999L)).isEmpty();
    }

    @Test
    void nameMustBeUnique() {
        create(draft("黑猫", "10000"));
        assertThatThrownBy(() -> create(draft("黑猫", "10004")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("存档名重复");
    }

    @Test
    void accountMustBeUnique() {
        create(draft("黑猫", "10000"));
        assertThatThrownBy(() -> create(draft("小明", "10000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("账号重复");
    }

    @Test
    void allAccountsHasChannelPrefix() {
        create(draft("黑猫", "10000"));
        assertThat(store.allAccounts()).contains("external_qq:10000");
    }

    @Test
    void updateKeepsIdAndCreatedAt() {
        Archive a = create(draft("黑猫", "10000"));
        Archive updated = store.update(a.id(), draft("黑猫2", "10000"));
        assertThat(updated.id()).isEqualTo(a.id());
        assertThat(updated.createdAt()).isEqualTo(a.createdAt());
        assertThat(updated.name()).isEqualTo("黑猫2");
    }

    @Test
    void updateMissingThrows() {
        assertThatThrownBy(() -> store.update(999L, draft("x", "1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteRemoves() throws IOException {
        Archive a = store.create(draft("黑猫", "10000"));
        Path dir = Path.of("data").resolve(Long.toString(a.id()));
        assertThat(dir).exists();

        store.delete(a.id());
        assertThat(store.find(a.id())).isEmpty();
        assertThat(dir).doesNotExist();
    }

    @Test
    void maxCountEnforced() {
        for (int i = 0; i < 4; i++) {
            create(draft("存档" + i, String.valueOf(10000 + i)));
        }
        assertThatThrownBy(() -> create(draft("存档5", "19999")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("上限");
    }

    @Test
    void enabledFiltersOutDisabled() {
        Archive a = create(draft("黑猫", "10000"));
        long before = store.enabled().stream().filter(x -> x.id() == a.id()).count();
        assertThat(before).isEqualTo(1);

        store.update(a.id(), new Archive("1", 0, "黑猫", false, null, null,
                a.links(), PipelineConfig.defaultConfig(), null, null));
        long after = store.enabled().stream().filter(x -> x.id() == a.id()).count();
        assertThat(after).isZero();
    }

    @Test
    void markSessionChangesStatus() {
        Archive a = create(draft("黑猫", "10000"));
        store.markSession(a.id(), Archive.SessionStatus.LOST);
        assertThat(store.find(a.id()).orElseThrow().session().status())
                .isEqualTo(Archive.SessionStatus.LOST);
    }

    @Test
    void registryReplaceAllThreeStates() {
        ArchiveRegistry reg = new ArchiveRegistry();
        assertThat(reg.replaceAll(null)).isFalse();

        assertThat(reg.replaceAll(Map.of())).isTrue();
        assertThat(reg.served()).isEmpty();

        var key = new com.wdd.cat.message.ChatKey(
                com.wdd.cat.message.ChatKey.ChatType.GROUP, "20001");
        var served = new ArchiveRegistry.ServedArchive("s1", "BOUND", Set.of(key));
        assertThat(reg.replaceAll(Map.of(1L, served))).isTrue();
        assertThat(reg.of(1L).orElseThrow().isBound()).isTrue();
        assertThat(reg.allChats()).containsExactly(key);
    }
}
