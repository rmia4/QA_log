package egovframework.project.gubun;

import java.util.Arrays;
import java.util.List;

public enum ProjectStatus {

    IN_PROGRESS("in_progress", "진행중"),
    MAINTENANCE("maintenance", "유지보수중"),
    ON_HOLD("on_hold", "보류"),
    ARCHIVED("archived", "보관");

    private final String code;
    private final String label;

    ProjectStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isArchived() {
        return this == ARCHIVED;
    }

    public static List<ProjectStatus> activeValues() {
        return Arrays.asList(IN_PROGRESS, MAINTENANCE, ON_HOLD);
    }

    public static ProjectStatus fromCode(String code) {
        for (ProjectStatus value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static String labelOf(String code) {
        ProjectStatus status = fromCode(code);
        return status == null ? code : status.label;
    }
}
