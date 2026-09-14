package egovframework.issue.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import egovframework.issue.dto.IssueCommentResponseDTO;
import egovframework.issue.exception.EmptyCommentException;
import egovframework.issue.exception.IssueForbiddenException;
import egovframework.issue.exception.IssueNotFoundException;
import egovframework.issue.gubun.IssueEventType;
import egovframework.issue.mapper.IssueAttachmentMapper;
import egovframework.issue.mapper.IssueCommentMapper;
import egovframework.issue.mapper.IssueHistoryMapper;
<<<<<<< HEAD
import egovframework.issue.mapper.IssueMapper;
=======
import egovframework.issue.service.AttachmentStorageService;
>>>>>>> origin/main
import egovframework.issue.service.IssueCommentService;
import egovframework.issue.service.StoredAttachment;
import egovframework.issue.vo.IssueAttachmentVO;
import egovframework.issue.vo.IssueCommentVO;
import egovframework.issue.vo.IssueHistoryVO;
import egovframework.project.service.ProjectService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class IssueCommentServiceImpl implements IssueCommentService {

    private final IssueCommentMapper issueCommentMapper;
    private final IssueHistoryMapper issueHistoryMapper;
<<<<<<< HEAD
    private final IssueMapper issueMapper;
    private final ProjectService projectService;
=======
    private final IssueAttachmentMapper issueAttachmentMapper;
    private final AttachmentStorageService attachmentStorageService;
>>>>>>> origin/main

    @Override
    @Transactional(readOnly = true)
    public List<IssueCommentResponseDTO> getComments(Long issueId) {
        List<IssueCommentResponseDTO> comments = issueCommentMapper.selectComments(issueId);
        Map<Long, List<IssueAttachmentVO>> attachmentsByCommentId = issueAttachmentMapper.selectCommentAttachments(issueId)
                .stream()
                .collect(Collectors.groupingBy(IssueAttachmentVO::getCommentId));
        for (IssueCommentResponseDTO comment : comments) {
            comment.setAttachments(attachmentsByCommentId.getOrDefault(comment.getId(), new ArrayList<>()));
        }
        return comments;
    }

    @Override
<<<<<<< HEAD
    public Long addComment(Long issueId, String content, Long actorId) {
        Long projectId = issueMapper.selectProjectId(issueId);
        if (projectId == null) {
            throw new IssueNotFoundException(issueId);
        }
        if (!projectService.isProjectAssignee(projectId, actorId)) {
            throw new IssueForbiddenException(issueId);
        }
=======
    public Long addComment(Long issueId, String content, List<MultipartFile> files, Long actorId) throws IOException {
>>>>>>> origin/main
        if (content == null || content.trim().isEmpty()) {
            throw new EmptyCommentException();
        }

        IssueCommentVO comment = new IssueCommentVO();
        comment.setIssueId(issueId);
        comment.setContent(content.trim());
        comment.setCreatedBy(actorId);
        issueCommentMapper.insertComment(comment);

        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                StoredAttachment stored = attachmentStorageService.store(issueId, file);
                IssueAttachmentVO attachment = new IssueAttachmentVO();
                attachment.setIssueId(issueId);
                attachment.setCommentId(comment.getId());
                attachment.setOriginalName(stored.getOriginalName());
                attachment.setStorageKey(stored.getStorageKey());
                attachment.setMimeType(stored.getMimeType());
                attachment.setSizeBytes(stored.getSizeBytes());
                attachment.setUploadedBy(actorId);
                issueAttachmentMapper.insertAttachment(attachment);
            }
        }

        // 감사 목적으로만 기록 - 변경 이력 탭에는 노출하지 않는다(IssueHistoryMapper.selectHistories에서 제외).
        // 댓글에 첨부한 스크린샷도 댓글 자체와 한 몸으로 취급해 별도 첨부 이력을 남기지 않는다.
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
