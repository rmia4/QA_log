package egovframework.issue.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * issue_histories 테이블 1:1 대응. event_type 값은 {@link IssueEventType} 참고.
 * old_value/new_value는 DB에는 JSON으로 저장하지만, 지금 범위(단순 문자열 필드 변경)에서는
 * 문자열을 그대로 담아 저장한다 - 값 자체가 JSON 배열/객체여야 하는 필드가 생기면 그때 확장한다.
 */
@Getter
@Setter
public class IssueHistoryVO {

    private Long id;
    private Long issueId;
    private String changeGroupId;
    private String eventType;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private Long relatedRecordId;
    private Long actorId;
    private LocalDateTime createdAt;
}
