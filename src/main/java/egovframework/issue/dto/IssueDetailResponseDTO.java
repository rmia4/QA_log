package egovframework.issue.dto;

import java.time.LocalDateTime;
import java.util.List;

import egovframework.common.KoreanDateTime;
import egovframework.issue.vo.IssueAttachmentVO;
import lombok.Getter;
import lombok.Setter;

/**
 * GET /issues/{id} 응답. issues 테이블 필드 + 화면 표시용 조인 결과(담당자명 등)를 한 번에 담는다.
 * 오류상세_화면명세서.md 2절(헤더/메타바/본문) 필드 구성과 1:1 대응.
 */
@Getter
@Setter
public class IssueDetailResponseDTO {

    private Long id;
    private Long issueNumber;
    private Long projectId;
    private String projectName;

    private String title;
    private String location;
    private String locationUrl;
    private String description;
    private String stepsToReproduce;
    private String expectedResult;
    private String actualResult;
    private String testVersion;
    private String testEnvironment;
    private String suggestedFix;

    private String status;
    private String severity;
    private String priority;

    private Long assigneeId;
    private String assigneeName;

    private Long createdBy;
    private String createdByName;
    private Long updatedBy;
    private String updatedByName;
    private Long closedBy;
    private String closedByName;
    private LocalDateTime closedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<IssueAttachmentVO> attachments;

    /**
     * 화면 표시용 - 한국 시간(KST) "yyyy-MM-dd HH:mm"만. 낙관적 잠금용 hidden expectedUpdatedAt
     * 필드에는 절대 쓰지 말 것(getUpdatedAt()의 원본 정밀도가 그대로 필요함) - KoreanDateTime 참고.
     */
    public String getCreatedAtDisplay() {
        return KoreanDateTime.format(createdAt);
    }

    public String getUpdatedAtDisplay() {
        return KoreanDateTime.format(updatedAt);
    }

    public String getClosedAtDisplay() {
        return KoreanDateTime.format(closedAt);
    }
}
