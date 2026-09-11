package egovframework.issue.dto.request;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueStatusChangeRequestDTO {
    /** IssueStatus 코드값 중 new/reviewing/fixing만 허용 - 종료/재오픈은 별도 API(close/reopen) 사용 */
    private String status;
    private LocalDateTime expectedUpdatedAt;
}
