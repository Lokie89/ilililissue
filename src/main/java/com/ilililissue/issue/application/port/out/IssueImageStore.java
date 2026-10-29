package com.ilililissue.issue.application.port.out;

import java.util.Optional;

/**
 * 이슈 대표 이미지(JPEG) 파일 저장소.
 */
public interface IssueImageStore {

    /**
     * 저장하고 새 키를 돌려준다. 같은 키를 다시 쓰지 않는다.
     */
    String save(byte[] jpeg);

    Optional<byte[]> load(String key);

    void delete(String key);
}
