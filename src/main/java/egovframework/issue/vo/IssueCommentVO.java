package egovframework.issue.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * issue_comments 테이블 1:1 대응.
 */
@Getter
@Setter
public class IssueCommentVO {

    private Long id;
    private Long issueId;
    private String content;
    private Long createdBy;
    private LocalDateTime createdAt;
}
