package egovframework.issue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 오류 등록자/처리 담당자만 가능한 동작(본문 수정, 상태변경, 종료, 재오픈, 담당자 재지정)을
 * 그 외 사용자가 시도했을 때. 담당자가 아직 없는(미지정) 오류의 담당자 지정은 예외적으로
 * 누구나 자기 자신을 지정하는 경우에 한해 허용한다(IssueServiceImpl 참고).
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class IssueForbiddenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IssueForbiddenException(Long issueId) {
        super("이 작업은 오류 등록자 또는 처리 담당자만 할 수 있습니다(issueId=" + issueId + ").");
    }
}
