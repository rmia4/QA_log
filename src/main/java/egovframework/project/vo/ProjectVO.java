package egovframework.project.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * projects 테이블 1:1 대응. writing-block.md 5절 참고.
 */
@Getter
@Setter
public class ProjectVO {

    private Long id;
    private String name;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 목록 화면에서 같이 보여줄 미종료 오류 수 - projects 테이블 컬럼이 아니라 조회 시 집계되는 값 */
    private Long openIssueCount;
}
