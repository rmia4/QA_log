package egovframework.issue.service.impl;

import java.io.IOException;
import java.nio.file.Files;
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
import egovframework.issue.dto.IssueInlineChangeResponseDTO;
import egovframework.issue.dto.request.IssueSaveRequestDTO;
import egovframework.issue.exception.InvalidIssueValueException;
import egovframework.issue.exception.IssueConflictException;
import egovframework.issue.exception.IssueForbiddenException;
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
import egovframework.project.mapper.ProjectMapper;
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
    private final ProjectMapper projectMapper;

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
    public Long createIssue(Long projectId, IssueSaveRequestDTO request, List<MultipartFile> files,
            List<MultipartFile> expectedResultFiles, List<MultipartFile> actualResultFiles, Long actorId)
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

        // issue_number는 DB 트리거(trg_issues_set_issue_number)가 선택한 프로젝트 안의 다음 번호로 채운다.
        issueMapper.insertIssue(issue);

        String changeGroupId = UUID.randomUUID().toString();
        recordEvent(issue.getId(), changeGroupId, IssueEventType.CREATED.getCode(), null, actorId);

        // 등록과 동시에 첨부한 파일은 별도 "첨부" 이력을 남기지 않는다 - "등록했다" 한 줄로 충분하고
        // 두 줄로 나뉘어 보이는 게 오히려 헷갈린다는 피드백으로 통합함(오류상세 화면 실사용 피드백).
        // 등록 이후 시점에 첨부가 추가되는 기능이 생기면 그때는 별도 이력을 남긴다.
        saveAllAttachmentFiles(issue.getId(), files, actorId, null);
        saveAllAttachmentFiles(issue.getId(), expectedResultFiles, actorId, "expected_result");
        saveAllAttachmentFiles(issue.getId(), actualResultFiles, actorId, "actual_result");

        return issue.getId();
    }

    private void saveAllAttachmentFiles(Long issueId, List<MultipartFile> files, Long actorId, String context)
            throws IOException {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            saveAttachmentFile(issueId, file, actorId, context);
        }
    }

    @Override
    public void updateIssueFields(Long id, IssueSaveRequestDTO request,
            List<MultipartFile> files, List<MultipartFile> expectedResultFiles, List<MultipartFile> actualResultFiles,
            List<Long> attachmentIdsToDelete, LocalDateTime expectedUpdatedAt, Long actorId) throws IOException {
        IssueDetailResponseDTO before = getIssueDetail(id);
        requireManagePermission(before, actorId);
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

        int affected = issueMapper.updateIssueFields(issue, expectedUpdatedAt);
        if (affected == 0) {
            throw new IssueConflictException(id);
        }

        for (IssueHistoryVO change : changes) {
            issueHistoryMapper.insertHistory(change);
        }

        deleteAttachments(id, attachmentIdsToDelete, changeGroupId, actorId);
        saveAttachmentsWithHistory(id, files, actorId, null, changeGroupId);
        saveAttachmentsWithHistory(id, expectedResultFiles, actorId, "expected_result", changeGroupId);
        saveAttachmentsWithHistory(id, actualResultFiles, actorId, "actual_result", changeGroupId);
    }

    /** 등록(createIssue)과 달리 수정 화면에서 추가한 첨부는 "첨부 추가" 이력을 남긴다(오류수정_기능명세서.md 2.2). */
    private void saveAttachmentsWithHistory(Long issueId, List<MultipartFile> files, Long actorId, String context,
            String changeGroupId) throws IOException {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            saveAttachment(issueId, changeGroupId, file, actorId, context);
        }
    }

    /** attachmentIdsToDelete에 있는 항목 중 실제로 이 issue 소속인 것만 삭제한다(다른 이슈 첨부 id는 조용히 무시). */
    private void deleteAttachments(Long issueId, List<Long> attachmentIdsToDelete, String changeGroupId, Long actorId)
            throws IOException {
        if (attachmentIdsToDelete == null) {
            return;
        }
        for (Long attachmentId : attachmentIdsToDelete) {
            IssueAttachmentVO attachment = issueAttachmentMapper.selectAttachmentById(attachmentId);
            if (attachment == null || !attachment.getIssueId().equals(issueId)) {
                continue;
            }
            Files.deleteIfExists(attachmentStorageService.resolve(issueId, attachment.getStorageKey()));
            issueAttachmentMapper.deleteAttachment(attachmentId);
            recordEvent(issueId, changeGroupId, IssueEventType.ATTACHMENT_REMOVED.getCode(), attachmentId, actorId);
        }
    }

    @Override
    public IssueInlineChangeResponseDTO changeSeverity(
            Long id, String severity, LocalDateTime expectedUpdatedAt, Long actorId) {
        requireValidValue("심각도", severity, IssueSeverity.isValid(severity));
        IssueDetailResponseDTO before = getIssueDetail(id);
        requireManagePermission(before, actorId);
        requireUpdated(id, issueMapper.updateSeverity(id, severity, actorId, expectedUpdatedAt));
        recordFieldChange(id, "severity", IssueSeverity.labelOf(before.getSeverity()),
                IssueSeverity.labelOf(severity), actorId);
        return inlineResponse(severity, IssueSeverity.labelOf(severity), id);
    }

    @Override
    public IssueInlineChangeResponseDTO changePriority(
            Long id, String priority, LocalDateTime expectedUpdatedAt, Long actorId) {
        requireValidValue("우선순위", priority, IssuePriority.isValid(priority));
        IssueDetailResponseDTO before = getIssueDetail(id);
        requireManagePermission(before, actorId);
        requireUpdated(id, issueMapper.updatePriority(id, priority, actorId, expectedUpdatedAt));
        recordFieldChange(id, "priority", IssuePriority.labelOf(before.getPriority()),
                IssuePriority.labelOf(priority), actorId);
        return inlineResponse(priority, IssuePriority.labelOf(priority), id);
    }

    @Override
    public IssueInlineChangeResponseDTO changeAssignee(
            Long id, Long assigneeId, LocalDateTime expectedUpdatedAt, Long actorId) {
        IssueDetailResponseDTO before = getIssueDetail(id);
        requireAssigneeChangePermission(before, actorId, assigneeId);
        String newAssigneeName = assigneeId == null ? "미지정" : userMapper.selectDisplayName(assigneeId);
        if (assigneeId != null && newAssigneeName == null) {
            throw new InvalidIssueValueException("담당자", assigneeId);
        }
        requireUpdated(id, issueMapper.updateAssignee(id, assigneeId, actorId, expectedUpdatedAt));
        recordFieldChange(id, "assignee_id", nullToUnspecified(before.getAssigneeName()), newAssigneeName, actorId);
        return inlineResponse(assigneeId == null ? "" : assigneeId.toString(), newAssigneeName, id);
    }

    @Override
    public IssueInlineChangeResponseDTO changeStatus(
            Long id, String status, LocalDateTime expectedUpdatedAt, Long actorId) {
        requireValidValue("상태", status, IssueStatus.isValid(status));
        IssueDetailResponseDTO before = getIssueDetail(id);
        requireManagePermission(before, actorId);
        requireUpdated(id, issueMapper.updateStatus(id, status, actorId, expectedUpdatedAt));
        recordFieldChange(id, "status", IssueStatus.labelOf(before.getStatus()), IssueStatus.labelOf(status), actorId);
        return inlineResponse(status, IssueStatus.labelOf(status), id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canManageIssue(Long id, Long actorId) {
        return canManage(getIssueDetail(id), actorId);
    }

    @Override
    public void closeIssue(Long id, LocalDateTime expectedUpdatedAt, Long actorId) {
        IssueDetailResponseDTO before = getIssueDetail(id);
        requireManagePermission(before, actorId);
        int affected = issueMapper.closeIssue(id, actorId, expectedUpdatedAt);
        if (affected == 0) {
            throw new IssueConflictException(id);
        }
        recordFieldChange(id, "status", IssueStatus.labelOf(before.getStatus()), IssueStatus.CLOSED.getLabel(), actorId);
    }

    @Override
    public void reopenIssue(Long id, LocalDateTime expectedUpdatedAt, Long actorId) {
        IssueDetailResponseDTO before = getIssueDetail(id);
        requireManagePermission(before, actorId);
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

    /** 첨부 파일 저장 + DB 행 기록만 한다(이력 없음) - 오류 등록 시점처럼 "created" 이력 하나로 충분한 경우. */
    private IssueAttachmentVO saveAttachmentFile(Long issueId, MultipartFile file, Long actorId, String context)
            throws IOException {
        StoredAttachment stored = attachmentStorageService.store(issueId, file);

        IssueAttachmentVO attachment = new IssueAttachmentVO();
        attachment.setIssueId(issueId);
        attachment.setOriginalName(stored.getOriginalName());
        attachment.setStorageKey(stored.getStorageKey());
        attachment.setMimeType(stored.getMimeType());
        attachment.setSizeBytes(stored.getSizeBytes());
        attachment.setUploadedBy(actorId);
        attachment.setContext(context);
        issueAttachmentMapper.insertAttachment(attachment);
        return attachment;
    }

    /** 첨부 저장 + "첨부 추가" 이력까지 남긴다 - 등록 이후(기존 오류에 첨부 추가) 시나리오에서 쓴다. */
    private void saveAttachment(Long issueId, String changeGroupId, MultipartFile file, Long actorId, String context)
            throws IOException {
        IssueAttachmentVO attachment = saveAttachmentFile(issueId, file, actorId, context);
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

    /**
     * 오류 등록자, 현재 처리 담당자 또는 프로젝트 담당자가 본문 수정/상태변경/종료/재오픈을 할 수 있다.
     * 담당자 재지정은 requireAssigneeChangePermission()의 별도 규칙(미지정 예외)을 따른다.
     */
    private void requireManagePermission(IssueDetailResponseDTO issue, Long actorId) {
        if (!canManage(issue, actorId)) {
            throw new IssueForbiddenException(issue.getId());
        }
    }

    private boolean canManage(IssueDetailResponseDTO issue, Long actorId) {
        return actorId != null
                && (actorId.equals(issue.getCreatedBy())
                || actorId.equals(issue.getAssigneeId())
                || projectMapper.existsProjectAssignee(issue.getProjectId(), actorId));
    }

    /**
     * 담당자 재지정은 기본적으로 등록자/현재 담당자만 가능하지만, 아직 담당자가 없는(미지정) 오류는
     * 예외적으로 누구나 "자기 자신"을 담당자로 지정할 수 있다 - 등록자·담당자가 둘 다 자리를 비우면
     * 새 오류가 영원히 미지정으로 남는 걸 막기 위한 안전장치(2026-09-14 팀 결정).
     */
    private void requireAssigneeChangePermission(IssueDetailResponseDTO issue, Long actorId, Long newAssigneeId) {
        if (actorId == null) {
            throw new IssueForbiddenException(issue.getId());
        }
        if (canManage(issue, actorId)) {
            return;
        }
        if (issue.getAssigneeId() == null && actorId.equals(newAssigneeId)) {
            return;
        }
        throw new IssueForbiddenException(issue.getId());
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

    private void requireUpdated(Long id, int affected) {
        if (affected == 0) {
            throw new IssueConflictException(id);
        }
    }

    private void requireValidValue(String fieldName, Object value, boolean valid) {
        if (!valid) {
            throw new InvalidIssueValueException(fieldName, value);
        }
    }

    private IssueInlineChangeResponseDTO inlineResponse(String value, String label, Long id) {
        return new IssueInlineChangeResponseDTO(value, label, issueMapper.selectIssueUpdatedAt(id));
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return (value == null || value.trim().isEmpty()) ? defaultValue : value;
    }

    private String nullToUnspecified(String value) {
        return value == null ? "미지정" : value;
    }
}
