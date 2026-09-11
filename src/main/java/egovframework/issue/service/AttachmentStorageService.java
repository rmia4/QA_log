package egovframework.issue.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface AttachmentStorageService {

    /**
     * 파일 내용을 매직 바이트로 검사해 실제 이미지 형식을 판별하고(허용 목록 밖이면
     * UnsupportedAttachmentTypeException), {issueId} 하위에 저장한다. 저장 파일 확장자는
     * 판별된 형식 기준이며 업로드자가 보낸 파일명은 원본 표시용으로만 DB에 남긴다.
     */
    StoredAttachment store(Long issueId, MultipartFile file) throws IOException;
}
