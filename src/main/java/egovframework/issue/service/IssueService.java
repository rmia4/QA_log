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

    /**
     * 오류등록_기능명세서.md 2.1 처리 순서(등록 -> 채번 -> created 이력) 그대로 구현한다. 등록 시점에
     * 첨부한 파일은 전부 하나의 "created" 이력으로 묶이고 별도 "첨부" 이력을 남기지 않는다(2026-09-11
     * 실사용 피드백). expectedResultFiles/actualResultFiles는 기대결과/실제결과 입력란 전용 첨부.
     */
    Long createIssue(Long projectId, IssueSaveRequestDTO request, List<MultipartFile> files,
            List<MultipartFile> expectedResultFiles, List<MultipartFile> actualResultFiles, Long actorId) throws IOException;

    /**
     * 실제로 값이 바뀐 필드만 골라 하나의 change_group_id로 이력을 남긴다(오류상세_기능명세서.md 4.2).
     * 신규 첨부(files/expectedResultFiles/actualResultFiles)와 기존 첨부 삭제(attachmentIdsToDelete)도
     * 같은 저장 요청으로 함께 처리한다(오류수정_기능명세서.md 2.2) - 등록과 달리 여기서는 첨부
     * 추가/삭제 각각 별도 이력을 남긴다. expectedUpdatedAt은 DTO가 아니라 별도 인자로 받는다 -
     * 브라우저 폼 제출(IssueViewController)에서는 String으로 받아 직접 파싱한 값을 넘기고,
     * JSON API(IssueController)에서는 request.getExpectedUpdatedAt()을 그대로 넘긴다.
     */
    void updateIssueFields(Long id, IssueSaveRequestDTO request,
            List<MultipartFile> files, List<MultipartFile> expectedResultFiles, List<MultipartFile> actualResultFiles,
            List<Long> attachmentIdsToDelete, LocalDateTime expectedUpdatedAt, Long actorId) throws IOException;

    void changeAssignee(Long id, Long assigneeId, LocalDateTime expectedUpdatedAt, Long actorId);

    void changeStatus(Long id, String status, LocalDateTime expectedUpdatedAt, Long actorId);

    void closeIssue(Long id, LocalDateTime expectedUpdatedAt, Long actorId);

    void reopenIssue(Long id, LocalDateTime expectedUpdatedAt, Long actorId);

    List<IssueHistoryResponseDTO> getHistories(Long id);
}
