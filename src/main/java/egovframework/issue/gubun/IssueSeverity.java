package egovframework.issue.gubun;

/**
 * issues.severity 코드값(버그가 얼마나 심각한가). writing-block.md 10절 참고.
 */
public enum IssueSeverity {

    UNSPECIFIED("unspecified", "미지정"),
    LOW("low", "낮음"),
    MEDIUM("medium", "보통"),
    HIGH("high", "높음"),
    CRITICAL("critical", "치명적");

    private final String code;
    private final String label;

    IssueSeverity(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static boolean isValid(String code) {
        for (IssueSeverity value : values()) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static String labelOf(String code) {
        for (IssueSeverity value : values()) {
            if (value.code.equals(code)) {
                return value.label;
            }
        }
        return code;
    }
}
