package egovframework.issue.service;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

public interface AttachmentStorageService {

    /**
     * 파일 내용을 매직 바이트로 검사해 실제 이미지 형식을 판별하고(허용 목록 밖이면
     * UnsupportedAttachmentTypeException), {issueId} 하위에 저장한다. 저장 파일 확장자는
     * 판별된 형식 기준이며 업로드자가 보낸 파일명은 원본 표시용으로만 DB에 남긴다.
     */
    StoredAttachment store(Long issueId, MultipartFile file) throws IOException;

    /**
     * 저장된 파일의 실제 경로를 구성한다. attachment.storage.path 설정값은 이 서비스가 속한
     * root-context에서만 @Value로 해석되므로(servlet-context에 속한 @Controller에는 property-
     * placeholder가 없어 "${attachment.storage.path}" 문자열이 그대로 들어가 파일을 못 찾는 버그가
     * 있었음 - 실제 첨부 다운로드 중 재현), 경로 조립은 항상 이 서비스를 통해서만 하도록 한다.
     */
    Path resolve(Long issueId, String storageKey);
}
