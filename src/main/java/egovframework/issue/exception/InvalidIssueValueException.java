package egovframework.issue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidIssueValueException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidIssueValueException(String fieldName, Object value) {
        super("허용되지 않은 " + fieldName + " 값입니다: " + value);
    }
}
