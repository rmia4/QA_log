package egovframework.issue.dto;

import java.time.LocalDateTime;

import egovframework.common.KoreanDateTime;
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

    public String getCreatedAtDisplay() {
        return KoreanDateTime.format(createdAt);
    }
}
