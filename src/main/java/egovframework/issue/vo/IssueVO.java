package egovframework.issue.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * issues 테이블 1:1 대응. writing-block.md 6절 + 오류 상세 화면 설계 합의(location_url, suggested_fix 포함) 참고.
 * 상세/등록 화면 구현 시 IssueMapper + issues.xml을 함께 채워 넣을 것.
 */
@Getter
@Setter
public class IssueVO {

    private Long id;
    private Long issueNumber;
    private Long projectId;
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
    private Long createdBy;
    private Long updatedBy;
    private Long closedBy;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
