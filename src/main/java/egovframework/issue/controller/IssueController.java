package egovframework.issue.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import egovframework.common.SessionKeys;
import egovframework.issue.dto.IssueDetailResponseDTO;
import egovframework.issue.dto.IssueHistoryResponseDTO;
import egovframework.issue.dto.request.IssueAssigneeChangeRequestDTO;
import egovframework.issue.dto.request.IssueConcurrencyRequestDTO;
import egovframework.issue.dto.request.IssueSaveRequestDTO;
import egovframework.issue.dto.request.IssueStatusChangeRequestDTO;
import egovframework.issue.service.IssueService;

/**
 * 오류상세_기능명세서.md §3 API 표 그대로 구현한다(JSON). /api 접두사를 붙여 화면 렌더링용
 * IssueViewController(같은 /issues/{id} 경로, JSP 반환)와 경로가 겹치지 않게 한다 -
 * ProjectController가 이미 쓰던 /api/projects 관례와 동일하게 맞춤.
 */
@RestController
@RequestMapping("/api")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @RequestMapping(value = "/issues/{id}", method = RequestMethod.GET)
    public IssueDetailResponseDTO detail(@PathVariable Long id) {
        return issueService.getIssueDetail(id);
    }

    @RequestMapping(value = "/projects/{projectId}/issues", method = RequestMethod.POST)
    public Long create(@PathVariable Long projectId,
            IssueSaveRequestDTO request,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            HttpSession session) throws IOException {
        Long actorId = currentUserId(session);
        return issueService.createIssue(projectId, request, files, actorId);
    }

    @RequestMapping(value = "/issues/{id}", method = RequestMethod.PUT)
    public void updateFields(@PathVariable Long id, @RequestBody IssueSaveRequestDTO request, HttpSession session) {
        issueService.updateIssueFields(id, request, currentUserId(session));
    }

    @RequestMapping(value = "/issues/{id}/assignee", method = RequestMethod.PUT)
    public void changeAssignee(@PathVariable Long id, @RequestBody IssueAssigneeChangeRequestDTO request,
            HttpSession session) {
        issueService.changeAssignee(id, request.getAssigneeId(), request.getExpectedUpdatedAt(), currentUserId(session));
    }

    @RequestMapping(value = "/issues/{id}/status", method = RequestMethod.PUT)
    public void changeStatus(@PathVariable Long id, @RequestBody IssueStatusChangeRequestDTO request,
            HttpSession session) {
        issueService.changeStatus(id, request.getStatus(), request.getExpectedUpdatedAt(), currentUserId(session));
    }

    @RequestMapping(value = "/issues/{id}/close", method = RequestMethod.POST)
    public void close(@PathVariable Long id, @RequestBody IssueConcurrencyRequestDTO request, HttpSession session) {
        issueService.closeIssue(id, request.getExpectedUpdatedAt(), currentUserId(session));
    }

    @RequestMapping(value = "/issues/{id}/reopen", method = RequestMethod.POST)
    public void reopen(@PathVariable Long id, @RequestBody IssueConcurrencyRequestDTO request, HttpSession session) {
        issueService.reopenIssue(id, request.getExpectedUpdatedAt(), currentUserId(session));
    }

    @RequestMapping(value = "/issues/{id}/histories", method = RequestMethod.GET)
    public List<IssueHistoryResponseDTO> histories(@PathVariable Long id) {
        return issueService.getHistories(id);
    }

    /**
     * TODO: 로그인 구현 완료 전까지는 세션에 아무것도 없어 null이 반환된다.
     * 동기 쪽 로그인 처리에서 성공 시 session.setAttribute(SessionKeys.LOGIN_USER_ID, userId)로
     * 맞춰주면 이 메서드가 바로 동작한다.
     */
    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(SessionKeys.LOGIN_USER_ID);
    }
}
