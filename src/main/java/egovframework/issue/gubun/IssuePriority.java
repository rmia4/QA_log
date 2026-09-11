package egovframework.issue.gubun;

/**
 * issues.priority 코드값(얼마나 급하게 고쳐야 하는가). writing-block.md 10절 참고.
 */
public enum IssuePriority {

    UNSPECIFIED("unspecified", "미지정"),
    LOW("low", "낮음"),
    NORMAL("normal", "보통"),
    HIGH("high", "높음"),
    URGENT("urgent", "긴급");

    private final String code;
    private final String label;

    IssuePriority(String code, String label) {
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
        for (IssuePriority value : values()) {
            if (value.code.equals(code)) {
                return value.label;
            }
        }
        return code;
    }
}
