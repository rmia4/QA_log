package egovframework.issue.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueListItemDTO {

    private Long id;
    private Long issueNumber;
    private Long projectId;
    private String title;
    private String status;
    private String severity;
    private String priority;
    private String assigneeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
