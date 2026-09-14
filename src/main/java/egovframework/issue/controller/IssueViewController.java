package egovframework.issue.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import egovframework.common.SessionKeys;
import egovframework.issue.dto.IssueDetailResponseDTO;
import egovframework.issue.dto.request.IssueSaveRequestDTO;
import egovframework.issue.exception.IssueConflictException;
import egovframework.issue.exception.IssueForbiddenException;
import egovframework.issue.gubun.IssuePriority;
import egovframework.issue.gubun.IssueSeverity;
import egovframework.issue.gubun.IssueStatus;
import egovframework.issue.mapper.IssueAttachmentMapper;
import egovframework.issue.service.AttachmentStorageService;
import egovframework.issue.service.IssueCommentService;
import egovframework.issue.service.IssueService;
import egovframework.issue.vo.IssueAttachmentVO;
import egovframework.project.service.ProjectService;
import egovframework.user.mapper.UserMapper;

/**
 * 오류 상세 화면(JSP) 렌더링 + 폼 액션(담당자변경/상태변경/종료/재오픈/댓글작성). JSON API(IssueController,
 * /api 접두사)와 달리 여기는 브라우저 폼 제출을 그대로 받아 Post-Redirect-Get으로 처리한다.
 * 오류상세_화면명세서.md 구조를 그대로 따른다.
 */
@Controller
@RequestMapping("/issues/{id}")
public class IssueViewController {

    @Autowired
    private IssueService issueService;
    @Autowired
    private IssueCommentService issueCommentService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private IssueAttachmentMapper issueAttachmentMapper;
    @Autowired
    private AttachmentStorageService attachmentStorageService;
    @Autowired
    private ProjectService projectService;

    @RequestMapping(method = RequestMethod.GET)
    public String detail(@PathVariable Long id, @RequestParam(required = false) String conflict,
            @RequestParam(required = false) String forbidden, Model model, HttpSession session) {
        IssueDetailResponseDTO issue = issueService.getIssueDetail(id);
        Long actorId = currentUserId(session);
        model.addAttribute("issue", issue);
        model.addAttribute("histories", issueService.getHistories(id));
        model.addAttribute("comments", issueCommentService.getComments(id));
        model.addAttribute("users", userMapper.selectAllForOptions());
        model.addAttribute("statusOptions", Arrays.asList(IssueStatus.NEW, IssueStatus.REVIEWING, IssueStatus.FIXING));
        model.addAttribute("conflict", conflict != null);
        model.addAttribute("forbidden", forbidden != null);
        // 오류 등록자/처리 담당자만 본문 수정·상태변경·종료·재오픈이 가능(2026-09-14 팀 결정) - 그 외에는
        // 관련 버튼/폼 자체를 화면에서 숨긴다. canClaimAssignee는 담당자가 아직 없는(미지정) 오류에 한해
        // 누구나 자기 자신을 담당자로 지정할 수 있는 예외(IssueServiceImpl.requireAssigneeChangePermission 참고).
        boolean canManage = canManage(issue, actorId);
        model.addAttribute("canManage", canManage);
        model.addAttribute("canClaimAssignee", canManage || issue.getAssigneeId() == null);
        return "issue/detail";
    }

    /** 오류수정_화면명세서.md - 등록 폼과 동일 레이아웃, 기존 값이 채워진 별도 페이지. */
    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        Long actorId = currentUserId(session);
        if (actorId == null) {
            return "redirect:/login";
        }
        IssueDetailResponseDTO issue = issueService.getIssueDetail(id);
        if (!canManage(issue, actorId)) {
            return "redirect:/issues/" + id + "?forbidden=true";
        }
        model.addAttribute("issue", issue);
        model.addAttribute("projects", projectService.getProjectList());
        model.addAttribute("severityOptions", Arrays.asList(IssueSeverity.values()));
        model.addAttribute("priorityOptions", Arrays.asList(IssuePriority.values()));
        return "issue/edit";
    }

    /**
     * 오류수정_기능명세서.md 2절 - 필드 수정 + 신규 첨부 추가 + 기존 첨부 삭제를 한 번에 처리한다.
     * expectedUpdatedAt 파라미터 이름을 "expectedUpdatedAtRaw"로 둔 이유: IssueSaveRequestDTO에도
     * 같은 이름의 LocalDateTime 프로퍼티가 있어서, 폼 필드명이 "expectedUpdatedAt"이면 스프링이
     * 커맨드 객체(request) 바인딩 과정에서도 같은 값을 그 프로퍼티에 자동 바인딩하려다 마이크로초
     * 정밀도 문자열을 못 파싱해 400을 낸다(실제 재현해서 발견) - 이름을 다르게 둬서 그 자동 바인딩
     * 자체가 아예 일어나지 않게 한다.
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public String update(@PathVariable Long id, IssueSaveRequestDTO request,
            @RequestParam String expectedUpdatedAtRaw,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "expectedResultFiles", required = false) List<MultipartFile> expectedResultFiles,
            @RequestParam(value = "actualResultFiles", required = false) List<MultipartFile> actualResultFiles,
            @RequestParam(value = "attachmentIdsToDelete", required = false) List<Long> attachmentIdsToDelete,
            HttpSession session) throws IOException {
        if (currentUserId(session) == null) {
            return "redirect:/login";
        }
        return withConflictHandling(id, () -> issueService.updateIssueFields(id, request,
                files, expectedResultFiles, actualResultFiles, attachmentIdsToDelete,
                parseUpdatedAt(expectedUpdatedAtRaw), currentUserId(session)));
    }

    @RequestMapping(value = "/comments", method = RequestMethod.POST)
    public String addComment(@PathVariable Long id, @RequestParam String content, HttpSession session) {
        if (currentUserId(session) == null) {
            return "redirect:/login";
        }
        issueCommentService.addComment(id, content, currentUserId(session));
        return "redirect:/issues/" + id;
    }

    @RequestMapping(value = "/assignee", method = RequestMethod.POST)
    public String changeAssignee(@PathVariable Long id, @RequestParam(required = false) String assigneeId,
            @RequestParam String expectedUpdatedAt, HttpSession session) throws IOException {
        if (currentUserId(session) == null) {
            return "redirect:/login";
        }
        // "미지정" 옵션은 빈 문자열로 전송된다 - Long으로 직접 바인딩하면 스프링이 "" -> Long 변환에서
        // 400을 낼 수 있어 String으로 받아 직접 파싱한다.
        Long parsedAssigneeId = (assigneeId == null || assigneeId.isEmpty()) ? null : Long.valueOf(assigneeId);
        return withConflictHandling(id, () ->
                issueService.changeAssignee(id, parsedAssigneeId, parseUpdatedAt(expectedUpdatedAt), currentUserId(session)));
    }

    @RequestMapping(value = "/status", method = RequestMethod.POST)
    public String changeStatus(@PathVariable Long id, @RequestParam String status,
            @RequestParam String expectedUpdatedAt, HttpSession session) throws IOException {
        if (currentUserId(session) == null) {
            return "redirect:/login";
        }
        return withConflictHandling(id, () ->
                issueService.changeStatus(id, status, parseUpdatedAt(expectedUpdatedAt), currentUserId(session)));
    }

    @RequestMapping(value = "/close", method = RequestMethod.POST)
    public String close(@PathVariable Long id, @RequestParam String expectedUpdatedAt, HttpSession session)
            throws IOException {
        if (currentUserId(session) == null) {
            return "redirect:/login";
        }
        return withConflictHandling(id, () ->
                issueService.closeIssue(id, parseUpdatedAt(expectedUpdatedAt), currentUserId(session)));
    }

    @RequestMapping(value = "/reopen", method = RequestMethod.POST)
    public String reopen(@PathVariable Long id, @RequestParam String expectedUpdatedAt, HttpSession session)
            throws IOException {
        if (currentUserId(session) == null) {
            return "redirect:/login";
        }
        return withConflictHandling(id, () ->
                issueService.reopenIssue(id, parseUpdatedAt(expectedUpdatedAt), currentUserId(session)));
    }

    /**
     * Postgres TIMESTAMP는 마이크로초(소수점 최대 6자리)까지 저장하는데, LocalDateTime.toString()이
     * 뒤쪽 0을 잘라내서 소수점 자릿수가 값마다 들쭉날쭉하다(예: ".566049" vs ".19228"). Spring MVC의
     * 기본 LocalDateTime 컨버터는 이 가변 자릿수를 못 받아들여 400을 낸다(mvn tomcat7:run 중 실제
     * 담당자 변경을 해보다가 재현) - @RequestParam LocalDateTime 자동 바인딩 대신 String으로 받아
     * LocalDateTime.parse()(ISO_LOCAL_DATE_TIME, 0~9자리 가변 지원)로 직접 파싱한다.
     */
    private LocalDateTime parseUpdatedAt(String value) {
        return LocalDateTime.parse(value);
    }

    /** 첨부 썸네일/다운로드 - 실제 파일은 웹앱 바깥(attachment.storage.path)에 있어 정적 리소스 매핑으로는 못 준다. */
    @RequestMapping(value = "/attachments/{attachmentId}", method = RequestMethod.GET)
    public void downloadAttachment(@PathVariable Long id, @PathVariable Long attachmentId,
            HttpServletResponse response) throws IOException {
        IssueAttachmentVO attachment = issueAttachmentMapper.selectAttachmentById(attachmentId);
        if (attachment == null || !attachment.getIssueId().equals(id)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Path filePath = attachmentStorageService.resolve(id, attachment.getStorageKey());
        response.setContentType(attachment.getMimeType());
        response.setHeader("Content-Disposition", "inline");
        Files.copy(filePath, response.getOutputStream());
    }

    /**
     * 낙관적 잠금 충돌(IssueConflictException)이나 권한 없음(IssueForbiddenException) 시
     * 화면을 깨뜨리는 대신 안내 문구와 함께 상세로 되돌아간다.
     */
    private String withConflictHandling(Long id, ThrowingRunnable action) throws IOException {
        try {
            action.run();
        } catch (IssueConflictException e) {
            return "redirect:/issues/" + id + "?conflict=true";
        } catch (IssueForbiddenException e) {
            return "redirect:/issues/" + id + "?forbidden=true";
        }
        return "redirect:/issues/" + id;
    }

    /** 오류 등록자 또는 현재 처리 담당자인지 - IssueServiceImpl.canManage()와 동일 규칙(간단해서 중복 허용). */
    private boolean canManage(IssueDetailResponseDTO issue, Long actorId) {
        return actorId != null
                && (actorId.equals(issue.getCreatedBy()) || actorId.equals(issue.getAssigneeId()));
    }

    /** updateIssueFields()가 IOException(체크 예외)을 던지므로 Runnable 대신 이걸 쓴다. */
    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws IOException;
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(SessionKeys.LOGIN_USER_ID);
    }
}
