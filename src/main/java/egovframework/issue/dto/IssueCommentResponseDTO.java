package egovframework.issue.dto;

import java.time.LocalDateTime;
import java.util.List;

import egovframework.common.KoreanDateTime;
import egovframework.issue.vo.IssueAttachmentVO;
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
    private List<IssueAttachmentVO> attachments;

    public String getCreatedAtDisplay() {
        return KoreanDateTime.format(createdAt);
    }
}
