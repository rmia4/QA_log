package egovframework.issue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 업로드된 파일의 실제 내용(매직 바이트)이 허용된 이미지 형식이 아닐 때 던진다.
 * 클라이언트가 보낸 파일명 확장자나 Content-Type 헤더는 신뢰하지 않는다 -
 * reference_project_mng.md에 기록된 확장자 위장 저장형 XSS 사례와 같은 문제를 막기 위함.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedAttachmentTypeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnsupportedAttachmentTypeException(String message) {
        super(message);
    }
}
