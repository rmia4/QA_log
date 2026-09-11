package egovframework.issue.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import egovframework.common.SessionKeys;
import egovframework.issue.exception.IssueConflictException;
import egovframework.issue.gubun.IssueStatus;
import egovframework.issue.mapper.IssueAttachmentMapper;
import egovframework.issue.service.IssueCommentService;
import egovframework.issue.service.IssueService;
import egovframework.issue.vo.IssueAttachmentVO;
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

    @Value("${attachment.storage.path}")
    private String storagePath;

    @RequestMapping(method = RequestMethod.GET)
    public String detail(@PathVariable Long id, @RequestParam(required = false) String conflict, Model model) {
        model.addAttribute("issue", issueService.getIssueDetail(id));
        model.addAttribute("histories", issueService.getHistories(id));
        model.addAttribute("comments", issueCommentService.getComments(id));
        model.addAttribute("users", userMapper.selectAllForOptions());
        model.addAttribute("statusOptions", Arrays.asList(IssueStatus.NEW, IssueStatus.REVIEWING, IssueStatus.FIXING));
        model.addAttribute("conflict", conflict != null);
        return "issue/detail";
    }

    @RequestMapping(value = "/comments", method = RequestMethod.POST)
    public String addComment(@PathVariable Long id, @RequestParam String content, HttpSession session) {
        issueCommentService.addComment(id, content, currentUserId(session));
        return "redirect:/issues/" + id;
    }

    @RequestMapping(value = "/assignee", method = RequestMethod.POST)
    public String changeAssignee(@PathVariable Long id, @RequestParam(required = false) String assigneeId,
            @RequestParam String expectedUpdatedAt, HttpSession session) {
        // "미지정" 옵션은 빈 문자열로 전송된다 - Long으로 직접 바인딩하면 스프링이 "" -> Long 변환에서
        // 400을 낼 수 있어 String으로 받아 직접 파싱한다.
        Long parsedAssigneeId = (assigneeId == null || assigneeId.isEmpty()) ? null : Long.valueOf(assigneeId);
        return withConflictHandling(id, () ->
                issueService.changeAssignee(id, parsedAssigneeId, parseUpdatedAt(expectedUpdatedAt), currentUserId(session)));
    }

    @RequestMapping(value = "/status", method = RequestMethod.POST)
    public String changeStatus(@PathVariable Long id, @RequestParam String status,
            @RequestParam String expectedUpdatedAt, HttpSession session) {
        return withConflictHandling(id, () ->
                issueService.changeStatus(id, status, parseUpdatedAt(expectedUpdatedAt), currentUserId(session)));
    }

    @RequestMapping(value = "/close", method = RequestMethod.POST)
    public String close(@PathVariable Long id, @RequestParam String expectedUpdatedAt, HttpSession session) {
        return withConflictHandling(id, () ->
                issueService.closeIssue(id, parseUpdatedAt(expectedUpdatedAt), currentUserId(session)));
    }

    @RequestMapping(value = "/reopen", method = RequestMethod.POST)
    public String reopen(@PathVariable Long id, @RequestParam String expectedUpdatedAt, HttpSession session) {
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
        Path filePath = Paths.get(storagePath, String.valueOf(id), attachment.getStorageKey());
        response.setContentType(attachment.getMimeType());
        response.setHeader("Content-Disposition", "inline");
        Files.copy(filePath, response.getOutputStream());
    }

    /** 낙관적 잠금 충돌(IssueConflictException) 시 화면을 깨뜨리는 대신 안내 문구와 함께 상세로 되돌아간다. */
    private String withConflictHandling(Long id, Runnable action) {
        try {
            action.run();
        } catch (IssueConflictException e) {
            return "redirect:/issues/" + id + "?conflict=true";
        }
        return "redirect:/issues/" + id;
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(SessionKeys.LOGIN_USER_ID);
    }
}
