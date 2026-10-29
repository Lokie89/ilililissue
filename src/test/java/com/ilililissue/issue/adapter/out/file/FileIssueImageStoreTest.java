package com.ilililissue.issue.adapter.out.file;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class FileIssueImageStoreTest {

    @TempDir
    Path dir;

    @DisplayName("저장하면 새 키를 주고, 그 키로 읽고 지운다")
    @Test
    void saveLoadDelete() {
        FileIssueImageStore store = new FileIssueImageStore(dir.toString());
        byte[] jpeg = {1, 2, 3};

        String key = store.save(jpeg);

        Assertions.assertThat(key).matches("^[a-f0-9]{32}$");
        Assertions.assertThat(store.load(key)).hasValue(jpeg);
        store.delete(key);
        Assertions.assertThat(store.load(key)).isEmpty();
    }

    @DisplayName("저장 형식이 아닌 키(경로 조작 등)는 읽지 않는다")
    @Test
    void rejectsInvalidKey() {
        FileIssueImageStore store = new FileIssueImageStore(dir.toString());

        Assertions.assertThat(store.load("../application")).isEmpty();
        Assertions.assertThat(store.load(null)).isEmpty();
    }
}
