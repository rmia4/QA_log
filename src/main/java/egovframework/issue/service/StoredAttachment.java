package egovframework.issue.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** AttachmentStorageService.store()의 결과 - 디스크에 실제로 쓴 파일 정보. */
@Getter
@AllArgsConstructor
public class StoredAttachment {
    private final String originalName;
    private final String storageKey;
    private final String mimeType;
    private final long sizeBytes;
}
