package egovframework.issue.gubun;

/**
 * issues.status 코드값. writing-block.md 10절 공통 코드값 참고.
 */
public enum IssueStatus {

    NEW("new", "신규"),
    REVIEWING("reviewing", "확인 중"),
    FIXING("fixing", "수정 중"),
    CLOSED("closed", "종료");

    private final String code;
    private final String label;

    IssueStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static String labelOf(String code) {
        for (IssueStatus value : values()) {
            if (value.code.equals(code)) {
                return value.label;
            }
        }
        return code;
    }
}
