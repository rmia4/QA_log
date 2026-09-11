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
}
