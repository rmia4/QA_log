package egovframework.issue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 낙관적 잠금 충돌 - UPDATE ... WHERE id=# AND updated_at=#{expectedUpdatedAt}의 영향받은 행이 0일 때
 * (다른 사람이 그 사이 먼저 수정함). 오류상세_기능명세서.md 5절 참고. 충돌 시 화면 UX는 아직 미정.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class IssueConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IssueConflictException(Long issueId) {
        super("다른 사용자가 먼저 수정했습니다(issueId=" + issueId + "). 화면을 새로고침한 뒤 다시 시도하세요.");
    }
}
