package egovframework.issue.dto;

import java.time.LocalDateTime;

import egovframework.common.KoreanDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueListItemDTO {

    private Long id;
    private Long issueNumber;
    private Long projectId;
    private String title;
    private String location;
    private String locationUrl;
    private String description;
    private String stepsToReproduce;
    private String expectedResult;
    private String actualResult;
    private String testVersion;
    private String testEnvironment;
    private String suggestedFix;
    private String status;
    private String severity;
    private String priority;
    private String assigneeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getCreatedAtDisplay() {
        return KoreanDateTime.format(createdAt);
    }

    public String getSearchText() {
        StringBuilder searchText = new StringBuilder();
        String[] values = {
                title, location, locationUrl, description, stepsToReproduce,
                expectedResult, actualResult, testVersion, testEnvironment, suggestedFix
        };
        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                if (searchText.length() > 0) {
                    searchText.append(' ');
                }
                searchText.append(value);
            }
        }
        return searchText.toString();
    }
}
