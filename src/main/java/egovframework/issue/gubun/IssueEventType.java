package egovframework.issue.gubun;

/**
 * issue_histories.event_type 코드값. writing-block.md 10절 참고.
 * comment_added는 감사 기록 용도로만 저장되고 변경 이력 탭에는 노출하지 않는다
 * (오류상세_기능명세서.md 4.4 참고 - IssueHistoryMapper.selectHistories에서 이 값을 제외한다).
 */
public enum IssueEventType {

    CREATED("created"),
    FIELD_CHANGED("field_changed"),
    ATTACHMENT_ADDED("attachment_added"),
    COMMENT_ADDED("comment_added");

    private final String code;

    IssueEventType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
