package egovframework.issue.service;

import java.util.List;

import egovframework.issue.dto.IssueCommentResponseDTO;

public interface IssueCommentService {

    List<IssueCommentResponseDTO> getComments(Long issueId);

    /** 공백만 있는 내용은 저장하지 않는다(오류상세_기능명세서.md 4.5). */
    Long addComment(Long issueId, String content, Long actorId);
}
