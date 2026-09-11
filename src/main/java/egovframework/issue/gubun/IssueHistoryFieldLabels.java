package egovframework.issue.gubun;

import java.util.HashMap;
import java.util.Map;

/**
 * issue_histories.field_name(DB 컬럼명 그대로 저장, writing-block.md 9절)을 화면 표시용 한글 라벨로
 * 바꾼다. 오류상세_기능명세서.md 4.4 "field_name → 한글 라벨 매핑" 표 구현.
 */
public final class IssueHistoryFieldLabels {

    private static final Map<String, String> LABELS = new HashMap<>();

    static {
        LABELS.put("title", "제목");
        LABELS.put("location", "발생 위치");
        LABELS.put("location_url", "URL 주소");
        LABELS.put("steps_to_reproduce", "재현 순서");
        LABELS.put("expected_result", "기대 결과");
        LABELS.put("actual_result", "실제 결과");
        LABELS.put("test_version", "테스트 버전");
        LABELS.put("test_environment", "테스트 환경");
        LABELS.put("suggested_fix", "개선 방향");
        LABELS.put("severity", "심각도");
        LABELS.put("priority", "우선순위");
        LABELS.put("assignee_id", "담당자");
        LABELS.put("status", "상태");
    }

    private IssueHistoryFieldLabels() {
    }

    public static String labelOf(String fieldName) {
        return LABELS.getOrDefault(fieldName, fieldName);
    }
}
