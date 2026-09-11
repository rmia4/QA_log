package egovframework.issue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmptyCommentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmptyCommentException() {
        super("빈 댓글은 등록할 수 없습니다");
    }
}
