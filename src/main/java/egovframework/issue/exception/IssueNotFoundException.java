package egovframework.issue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class IssueNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IssueNotFoundException(Long issueId) {
        super("존재하지 않는 오류입니다(issueId=" + issueId + ")");
    }
}
