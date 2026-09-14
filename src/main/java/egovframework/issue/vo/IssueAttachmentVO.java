package egovframework.issue.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * issue_attachments 테이블 1:1 대응.
 */
@Getter
@Setter
public class IssueAttachmentVO {

    private Long id;
    private Long issueId;
    private String originalName;
    private String storageKey;
    private String mimeType;
    private Long sizeBytes;
    private Long uploadedBy;
    private LocalDateTime createdAt;

    /** null=오류 본문 전체에 딸린 일반 첨부, "expected_result"/"actual_result"=해당 필드 전용 첨부 */
    private String context;

    /** null=오류 본문에 딸린 첨부, not null=해당 댓글 전용 첨부(댓글 id) */
    private Long commentId;
}
