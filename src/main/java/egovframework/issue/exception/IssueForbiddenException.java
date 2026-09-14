package egovframework.issue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 오류 또는 소속 프로젝트에서 허용되지 않은 쓰기 작업을 시도했을 때 발생한다. */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class IssueForbiddenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IssueForbiddenException(Long issueId) {
        super("이 작업을 수행할 권한이 없습니다(issueId=" + issueId + ").");
    }
}
