package egovframework.issue.dto;

import java.time.LocalDateTime;

import egovframework.common.KoreanJosa;
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

    /** oldValue가 없으면 화면 표시용 "미지정" */
    public String getOldValueDisplay() {
        return (oldValue == null || oldValue.isEmpty()) ? "미지정" : oldValue;
    }

    /** newValue가 없으면 화면 표시용 "미지정" */
    public String getNewValueDisplay() {
        return (newValue == null || newValue.isEmpty()) ? "미지정" : newValue;
    }

    /** "{fieldLabel}{을|를}" 조합용 조사 - 받침 유무에 맞게 자동 선택(KoreanJosa) */
    public String getFieldJosaEul() {
        return KoreanJosa.eulReul(getFieldLabel());
    }

    /** "{newValueDisplay}{로|으로}" 조합용 조사 */
    public String getNewValueJosaRo() {
        return KoreanJosa.roEuro(getNewValueDisplay());
    }
}
