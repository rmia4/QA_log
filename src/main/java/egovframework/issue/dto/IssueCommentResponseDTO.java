package egovframework.issue.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueCommentResponseDTO {

    private Long id;
    private String content;
    private Long createdBy;
    private String authorName;
    private LocalDateTime createdAt;
}
