package egovframework.issue.dto.request;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueAssigneeChangeRequestDTO {
    private Long assigneeId;
    private LocalDateTime expectedUpdatedAt;
}
