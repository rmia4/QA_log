package egovframework.issue.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueCommentSaveRequestDTO {

    /** 공백만 있는 값은 서비스 계층에서 저장하지 않고 400 처리(오류상세_기능명세서.md 4.5). */
    private String content;
}
