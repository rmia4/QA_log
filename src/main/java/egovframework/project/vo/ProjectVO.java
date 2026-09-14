package egovframework.project.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import egovframework.project.gubun.ProjectStatus;

/**
 * projects 테이블과 프로젝트 목록 표시용 집계 값을 담는다.
 */
@Getter
@Setter
public class ProjectVO {

    private Long id;
    private String name;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 목록 화면에서 같이 보여줄 미종료 오류 수 - projects 테이블 컬럼이 아니라 조회 시 집계되는 값 */
    private Long openIssueCount;

    /** project_assignees 연결 테이블에서 조회한 담당자 ID 목록 */
    private List<Long> assigneeIds;

    /** 좌측 프로젝트 목록 표시용 담당자 이름(쉼표 구분) */
    private String assigneeNames;

    public String getStatusLabel() {
        return ProjectStatus.labelOf(status);
    }
}
