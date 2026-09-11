package egovframework.issue.dto;

import java.time.LocalDateTime;

import egovframework.issue.gubun.IssueHistoryFieldLabels;
import lombok.Getter;
import lombok.Setter;

/**
 * GET /issues/{id}/histories 응답 1건. 화면 표시 문구 조립은 프론트(또는 필요하면 서비스 계층)에서
 * eventType/fieldName/oldValue/newValue를 조합해 처리한다 - 오류상세_기능명세서.md 4.4 표 참고.
 */
@Getter
@Setter
public class IssueHistoryResponseDTO {

    private Long id;
    private String changeGroupId;
    private String eventType;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private Long relatedRecordId;
    private Long actorId;
    private String actorName;
    private LocalDateTime createdAt;

    /** 화면(JSP)에서 바로 쓰는 계산 값 - field_name(DB 컬럼명)을 한글 라벨로. */
    public String getFieldLabel() {
        return IssueHistoryFieldLabels.labelOf(fieldName);
    }
}
