package egovframework.issue.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import egovframework.issue.dto.IssueDetailResponseDTO;
import egovframework.issue.dto.IssueHistoryResponseDTO;
import egovframework.issue.dto.request.IssueSaveRequestDTO;

public interface IssueService {

    IssueDetailResponseDTO getIssueDetail(Long id);

    /** 오류등록_기능명세서.md 2.1 처리 순서(등록 -> 채번 -> created 이력 -> 첨부 각각 저장+이력) 그대로 구현한다. */
    Long createIssue(Long projectId, IssueSaveRequestDTO request, List<MultipartFile> files, Long actorId) throws IOException;

    /** 실제로 값이 바뀐 필드만 골라 하나의 change_group_id로 이력을 남긴다(오류상세_기능명세서.md 4.2). */
    void updateIssueFields(Long id, IssueSaveRequestDTO request, Long actorId);

    void changeAssignee(Long id, Long assigneeId, LocalDateTime expectedUpdatedAt, Long actorId);

    void changeStatus(Long id, String status, LocalDateTime expectedUpdatedAt, Long actorId);

    void closeIssue(Long id, LocalDateTime expectedUpdatedAt, Long actorId);

    void reopenIssue(Long id, LocalDateTime expectedUpdatedAt, Long actorId);

    List<IssueHistoryResponseDTO> getHistories(Long id);
}
