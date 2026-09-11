package egovframework.issue.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 오류 등록(POST /projects/{projectId}/issues)과 본문 수정(PUT /issues/{id})이 공유하는 요청 필드.
 * 오류등록_화면명세서.md 2절 참고 - 전 항목 선택 입력이라 검증 애노테이션(@NotBlank 등)을 두지 않는다.
 */
@Getter
@Setter
public class IssueSaveRequestDTO {

    private String title;
    private String location;
    private String locationUrl;
    private String stepsToReproduce;
    private String expectedResult;
    private String actualResult;
    private String testVersion;
    private String testEnvironment;
    private String severity;
    private String priority;
    private String suggestedFix;
    private Long assigneeId;

    /** 낙관적 잠금 확인용 - 수정 화면 진입 시점의 updated_at을 그대로 되돌려받는다. 등록 시에는 사용하지 않음(null). */
    private java.time.LocalDateTime expectedUpdatedAt;
}
