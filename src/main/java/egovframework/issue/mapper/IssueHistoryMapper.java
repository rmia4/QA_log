package egovframework.issue.mapper;

import java.util.List;

import egovframework.issue.dto.IssueHistoryResponseDTO;
import egovframework.issue.vo.IssueHistoryVO;

public interface IssueHistoryMapper {

    void insertHistory(IssueHistoryVO history);

    /** comment_added는 댓글 탭과 중복 노출을 막기 위해 이 목록에서 제외한다(issues.xml에서 처리). */
    List<IssueHistoryResponseDTO> selectHistories(Long issueId);
}
