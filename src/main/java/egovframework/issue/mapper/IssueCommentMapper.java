package egovframework.issue.mapper;

import java.util.List;

import egovframework.issue.dto.IssueCommentResponseDTO;
import egovframework.issue.vo.IssueCommentVO;

public interface IssueCommentMapper {

    void insertComment(IssueCommentVO comment);

    List<IssueCommentResponseDTO> selectComments(Long issueId);
}
