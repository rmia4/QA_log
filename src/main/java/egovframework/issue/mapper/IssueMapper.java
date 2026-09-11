package egovframework.issue.mapper;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Param;

import egovframework.issue.dto.IssueDetailResponseDTO;
import egovframework.issue.vo.IssueVO;

public interface IssueMapper {

    /** 담당자명·등록자명·프로젝트명까지 조인해서 화면 하나 그리는 데 필요한 전부를 반환. 첨부목록은 별도 쿼리(IssueAttachmentMapper). */
    IssueDetailResponseDTO selectIssueDetail(Long id);

    /** 등록. useGeneratedKeys로 issue.getId()에 생성된 PK가 채워진다. */
    void insertIssue(IssueVO issue);

    /**
     * 본문 필드 일괄 수정. WHERE id=#{issue.id} AND updated_at=#{expectedUpdatedAt}로 낙관적 잠금 확인 -
     * 영향받은 행이 0이면(다른 사람이 먼저 수정) 서비스 계층에서 충돌로 판단한다.
     */
    int updateIssueFields(@Param("issue") IssueVO issue, @Param("expectedUpdatedAt") LocalDateTime expectedUpdatedAt);

    int updateAssignee(@Param("id") Long id, @Param("assigneeId") Long assigneeId,
            @Param("updatedBy") Long updatedBy, @Param("expectedUpdatedAt") LocalDateTime expectedUpdatedAt);

    int updateStatus(@Param("id") Long id, @Param("status") String status,
            @Param("updatedBy") Long updatedBy, @Param("expectedUpdatedAt") LocalDateTime expectedUpdatedAt);

    int closeIssue(@Param("id") Long id, @Param("closedBy") Long closedBy,
            @Param("expectedUpdatedAt") LocalDateTime expectedUpdatedAt);

    int reopenIssue(@Param("id") Long id, @Param("updatedBy") Long updatedBy,
            @Param("expectedUpdatedAt") LocalDateTime expectedUpdatedAt);
}
