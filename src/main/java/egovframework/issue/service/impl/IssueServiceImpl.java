package egovframework.issue.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import egovframework.issue.dto.IssueDetailResponseDTO;
import egovframework.issue.dto.IssueHistoryResponseDTO;
import egovframework.issue.dto.request.IssueSaveRequestDTO;
import egovframework.issue.exception.IssueConflictException;
import egovframework.issue.exception.IssueNotFoundException;
import egovframework.issue.gubun.IssueEventType;
import egovframework.issue.gubun.IssuePriority;
import egovframework.issue.gubun.IssueSeverity;
import egovframework.issue.gubun.IssueStatus;
import egovframework.issue.mapper.IssueAttachmentMapper;
import egovframework.issue.mapper.IssueHistoryMapper;
import egovframework.issue.mapper.IssueMapper;
import egovframework.issue.service.AttachmentStorageService;
import egovframework.issue.service.IssueService;
import egovframework.issue.service.StoredAttachment;
import egovframework.issue.vo.IssueAttachmentVO;
import egovframework.issue.vo.IssueHistoryVO;
import egovframework.issue.vo.IssueVO;
import egovframework.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private final IssueMapper issueMapper;
    private final IssueHistoryMapper issueHistoryMapper;
    private final IssueAttachmentMapper issueAttachmentMapper;
    private final AttachmentStorageService attachmentStorageService;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public IssueDetailResponseDTO getIssueDetail(Long id) {
        IssueDetailResponseDTO detail = issueMapper.selectIssueDetail(id);
        if (detail == null) {
            throw new IssueNotFoundException(id);
        }
        detail.setAttachments(issueAttachmentMapper.selectAttachments(id));
        return detail;
    }

    @Override
    public Long createIssue(Long projectId, IssueSaveRequestDTO request, List<MultipartFile> files, Long actorId)
            throws IOException {
        IssueVO issue = new IssueVO();
        issue.setProjectId(projectId);
        issue.setTitle(request.getTitle());
        issue.setLocation(request.getLocation());
        issue.setLocationUrl(request.getLocationUrl());
        issue.setStepsToReproduce(request.getStepsToReproduce());
        issue.setExpectedResult(request.getExpectedResult());
        issue.setActualResult(request.getActualResult());
        issue.setTestVersion(request.getTestVersion());
        issue.setTestEnvironment(request.getTestEnvironment());
        issue.setSuggestedFix(request.getSuggestedFix());
        issue.setStatus(IssueStatus.NEW.getCode());
        issue.setSeverity(defaultIfBlank(request.getSeverity(), IssueSeverity.UNSPECIFIED.getCode()));
        issue.setPriority(defaultIfBlank(request.getPriority(), IssuePriority.UNSPECIFIED.getCode()));
        issue.setAssigneeId(request.getAssigneeId());
        issue.setCreatedBy(actorId);
        issue.setUpdatedBy(actorId);

        issueMapper.insertIssue(issue);
        issueMapper.syncIssueNumber(issue.getId());

        String changeGroupId = UUID.randomUUID().toString();
        recordEvent(issue.getId(), changeGroupId, IssueEventType.CREATED.getCode(), null, actorId);

        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                saveAttachment(issue.getId(), changeGroupId, file, actorId);
            }
        }

        return issue.getId();
    }

    @Override
    public void updateIssueFields(Long id, IssueSaveRequestDTO request, Long actorId) {
        IssueDetailResponseDTO before = getIssueDetail(id);
        String changeGroupId = UUID.randomUUID().toString();

        // severity/priority는 빈 값이 오면 "미지정"으로 저장하므로, 이력 비교와 실제 저장값 모두
        // 이 defaulted 값을 기준으로 삼는다(그렇지 않으면 이력엔 null이 남고 DB엔 "미지정"이 저장되는
        // 불일치가 생긴다).
        String newSeverity = defaultIfBlank(request.getSeverity(), IssueSeverity.UNSPECIFIED.getCode());
        String newPriority = defaultIfBlank(request.getPriority(), IssuePriority.UNSPECIFIED.getCode());

        List<IssueHistoryVO> changes = new ArrayList<>();
        addIfChanged(changes, id, changeGroupId, "title", before.getTitle(), request.getTitle(), actorId);
        addIfChanged(changes, id, changeGroupId, "location", before.getLocation(), request.getLocation(), actorId);
        addIfChanged(changes, id, changeGroupId, "location_url", before.getLocationUrl(), request.getLocationUrl(), actorId);
        addIfChanged(changes, id, changeGroupId, "steps_to_reproduce", before.getStepsToReproduce(), request.getStepsToReproduce(), actorId);
        addIfChanged(changes, id, changeGroupId, "expected_result", before.getExpectedResult(), request.getExpectedResult(), actorId);
        addIfChanged(changes, id, changeGroupId, "actual_result", before.getActualResult(), request.getActualResult(), actorId);
        addIfChanged(changes, id, changeGroupId, "test_version", before.getTestVersion(), request.getTestVersion(), actorId);
        addIfChanged(changes, id, changeGroupId, "test_environment", before.getTestEnvironment(), request.getTestEnvironment(), actorId);
        addIfChanged(changes, id, changeGroupId, "suggested_fix", before.getSuggestedFix(), request.getSuggestedFix(), actorId);
        addIfChanged(changes, id, changeGroupId, "severity",
                IssueSeverity.labelOf(before.getSeverity()), IssueSeverity.labelOf(newSeverity), actorId);
        addIfChanged(changes, id, changeGroupId, "priority",
                IssuePriority.labelOf(before.getPriority()), IssuePriority.labelOf(newPriority), actorId);

        IssueVO issue = new IssueVO();
        issue.setId(id);
        issue.setTitle(request.getTitle());
        issue.setLocation(request.getLocation());
        issue.setLocationUrl(request.getLocationUrl());
        issue.setStepsToReproduce(request.getStepsToReproduce());
        issue.setExpectedResult(request.getExpectedResult());
        issue.setActualResult(request.getActualResult());
        issue.setTestVersion(request.getTestVersion());
        issue.setTestEnvironment(request.getTestEnvironment());
        issue.setSuggestedFix(request.getSuggestedFix());
        issue.setSeverity(newSeverity);
        issue.setPriority(newPriority);
        issue.setUpdatedBy(actorId);

        int affected = issueMapper.updateIssueFields(issue, request.getExpectedUpdatedAt());
        if (affected == 0) {
            throw new IssueConflictException(id);
        }

        for (IssueHistoryVO change : changes) {
            issueHistoryMapper.insertHistory(change);
        }
    }

    @Override
    public void changeAssignee(Long id, Long assigneeId, LocalDateTime expectedUpdatedAt, Long actorId) {
        IssueDetailResponseDTO before = getIssueDetail(id);
        int affected = issueMapper.updateAssignee(id, assigneeId, actorId, expectedUpdatedAt);
        if (affected == 0) {
            throw new IssueConflictException(id);
        }
        String newAssigneeName = assigneeId == null ? "미지정" : userMapper.selectDisplayName(assigneeId);
        recordFieldChange(id, "assignee_id", nullToUnspecified(before.getAssigneeName()), newAssigneeName, actorId);
    }

    @Override
    public void changeStatus(Long id, String status, LocalDateTime expectedUpdatedAt, Long actorId) {
        IssueDetailResponseDTO before = getIssueDetail(id);
        int affected = issueMapper.updateStatus(id, status, actorId, expectedUpdatedAt);
        if (affected == 0) {
            throw new IssueConflictException(id);
        }
        recordFieldChange(id, "status", IssueStatus.labelOf(before.getStatus()), IssueStatus.labelOf(status), actorId);
    }

    @Override
    public void closeIssue(Long id, LocalDateTime expectedUpdatedAt, Long actorId) {
        IssueDetailResponseDTO before = getIssueDetail(id);
        int affected = issueMapper.closeIssue(id, actorId, expectedUpdatedAt);
        if (affected == 0) {
            throw new IssueConflictException(id);
        }
        recordFieldChange(id, "status", IssueStatus.labelOf(before.getStatus()), IssueStatus.CLOSED.getLabel(), actorId);
    }

    @Override
    public void reopenIssue(Long id, LocalDateTime expectedUpdatedAt, Long actorId) {
        IssueDetailResponseDTO before = getIssueDetail(id);
        int affected = issueMapper.reopenIssue(id, actorId, expectedUpdatedAt);
        if (affected == 0) {
            throw new IssueConflictException(id);
        }
        recordFieldChange(id, "status", IssueStatus.labelOf(before.getStatus()), IssueStatus.NEW.getLabel(), actorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueHistoryResponseDTO> getHistories(Long id) {
        return issueHistoryMapper.selectHistories(id);
    }

    private void saveAttachment(Long issueId, String changeGroupId, MultipartFile file, Long actorId) throws IOException {
        StoredAttachment stored = attachmentStorageService.store(issueId, file);

        IssueAttachmentVO attachment = new IssueAttachmentVO();
        attachment.setIssueId(issueId);
        attachment.setOriginalName(stored.getOriginalName());
        attachment.setStorageKey(stored.getStorageKey());
        attachment.setMimeType(stored.getMimeType());
        attachment.setSizeBytes(stored.getSizeBytes());
        attachment.setUploadedBy(actorId);
        issueAttachmentMapper.insertAttachment(attachment);

        recordEvent(issueId, changeGroupId, IssueEventType.ATTACHMENT_ADDED.getCode(), attachment.getId(), actorId);
    }

    private void recordEvent(Long issueId, String changeGroupId, String eventType, Long relatedRecordId, Long actorId) {
        IssueHistoryVO history = new IssueHistoryVO();
        history.setIssueId(issueId);
        history.setChangeGroupId(changeGroupId);
        history.setEventType(eventType);
        history.setRelatedRecordId(relatedRecordId);
        history.setActorId(actorId);
        issueHistoryMapper.insertHistory(history);
    }

    private void recordFieldChange(Long issueId, String fieldName, String oldValue, String newValue, Long actorId) {
        IssueHistoryVO history = new IssueHistoryVO();
        history.setIssueId(issueId);
        history.setChangeGroupId(UUID.randomUUID().toString());
        history.setEventType(IssueEventType.FIELD_CHANGED.getCode());
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setActorId(actorId);
        issueHistoryMapper.insertHistory(history);
    }

    private void addIfChanged(List<IssueHistoryVO> changes, Long issueId, String changeGroupId, String fieldName,
            String oldValue, String newValue, Long actorId) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }
        IssueHistoryVO history = new IssueHistoryVO();
        history.setIssueId(issueId);
        history.setChangeGroupId(changeGroupId);
        history.setEventType(IssueEventType.FIELD_CHANGED.getCode());
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setActorId(actorId);
        changes.add(history);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return (value == null || value.trim().isEmpty()) ? defaultValue : value;
    }

    private String nullToUnspecified(String value) {
        return value == null ? "미지정" : value;
    }
}
