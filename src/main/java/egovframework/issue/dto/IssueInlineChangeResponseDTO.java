package egovframework.issue.dto;

import java.time.LocalDateTime;

import egovframework.common.KoreanDateTime;
import lombok.Getter;

@Getter
public class IssueInlineChangeResponseDTO {

    private final String value;
    private final String label;
    private final String updatedAt;
    private final String updatedAtDisplay;

    public IssueInlineChangeResponseDTO(String value, String label, LocalDateTime updatedAt) {
        this.value = value;
        this.label = label;
        this.updatedAt = updatedAt.toString();
        this.updatedAtDisplay = KoreanDateTime.format(updatedAt);
    }
}
