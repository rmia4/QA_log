package egovframework.issue.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.issue.dto.IssueCommentResponseDTO;
import egovframework.issue.exception.EmptyCommentException;
import egovframework.issue.gubun.IssueEventType;
import egovframework.issue.mapper.IssueCommentMapper;
import egovframework.issue.mapper.IssueHistoryMapper;
import egovframework.issue.service.IssueCommentService;
import egovframework.issue.vo.IssueCommentVO;
import egovframework.issue.vo.IssueHistoryVO;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class IssueCommentServiceImpl implements IssueCommentService {

    private final IssueCommentMapper issueCommentMapper;
    private final IssueHistoryMapper issueHistoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<IssueCommentResponseDTO> getComments(Long issueId) {
        return issueCommentMapper.selectComments(issueId);
    }

    @Override
    public Long addComment(Long issueId, String content, Long actorId) {
        if (content == null || content.trim().isEmpty()) {
            throw new EmptyCommentException();
        }

        IssueCommentVO comment = new IssueCommentVO();
        comment.setIssueId(issueId);
        comment.setContent(content.trim());
        comment.setCreatedBy(actorId);
        issueCommentMapper.insertComment(comment);

        // 감사 목적으로만 기록 - 변경 이력 탭에는 노출하지 않는다(IssueHistoryMapper.selectHistories에서 제외)
        IssueHistoryVO history = new IssueHistoryVO();
        history.setIssueId(issueId);
        history.setChangeGroupId(UUID.randomUUID().toString());
        history.setEventType(IssueEventType.COMMENT_ADDED.getCode());
        history.setRelatedRecordId(comment.getId());
        history.setActorId(actorId);
        issueHistoryMapper.insertHistory(history);

        return comment.getId();
    }
}
