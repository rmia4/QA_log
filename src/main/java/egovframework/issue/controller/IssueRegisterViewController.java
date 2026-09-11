package egovframework.issue.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
import egovframework.issue.dto.request.IssueSaveRequestDTO;
import egovframework.issue.gubun.IssuePriority;
import egovframework.issue.gubun.IssueSeverity;
import egovframework.issue.service.IssueService;
import egovframework.project.service.ProjectService;
import egovframework.user.mapper.UserMapper;

/**
 * 오류 등록 화면(JSP) 렌더링 + 폼 제출 처리. 오류등록_화면명세서.md / 오류등록_기능명세서.md 참고.
 * JSON API(POST /api/projects/{projectId}/issues, IssueController)와 별개로 브라우저 폼 제출을
 * Post-Redirect-Get으로 처리한다(오류상세와 동일 패턴, IssueViewController 참고).
 */
@Controller
@RequestMapping("/projects/{projectId}/issues")
public class IssueRegisterViewController {

    @Autowired
    private IssueService issueService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserMapper userMapper;

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String form(@PathVariable Long projectId, Model model, HttpSession session) {
        if (session.getAttribute(SessionKeys.LOGIN_USER_ID) == null) {
            return "redirect:/login";
        }
        model.addAttribute("projects", projectService.getProjectList());
        model.addAttribute("project", projectService.getProject(projectId));
        model.addAttribute("selectedProjectId", projectId);
        String registrationToken = UUID.randomUUID().toString();
        session.setAttribute(SessionKeys.ISSUE_CREATE_TOKEN, registrationToken);
        model.addAttribute("registrationToken", registrationToken);
        model.addAttribute("users", userMapper.selectAllForOptions());
        model.addAttribute("severityOptions", Arrays.asList(IssueSeverity.values()));
        model.addAttribute("priorityOptions", Arrays.asList(IssuePriority.values()));
        return "issue/register";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String create(@PathVariable Long projectId,
            IssueSaveRequestDTO request,
            @RequestParam(value = "registrationToken", required = false) String registrationToken,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            HttpSession session) throws IOException {
        Long actorId = (Long) session.getAttribute(SessionKeys.LOGIN_USER_ID);
        if (actorId == null) {
            return "redirect:/login";
        }
        if (!consumeRegistrationToken(session, registrationToken)) {
            return "redirect:/?projectId=" + projectId + "&issueCreateError=duplicate";
        }

        boolean created = false;
        try {
            Long issueId = issueService.createIssue(projectId, request, files, actorId);
            created = true;
            return "redirect:/issues/" + issueId;
        } finally {
            if (!created) {
                restoreRegistrationToken(session, registrationToken);
            }
        }
    }

    private boolean consumeRegistrationToken(HttpSession session, String registrationToken) {
        synchronized (session) {
            Object savedToken = session.getAttribute(SessionKeys.ISSUE_CREATE_TOKEN);
            if (registrationToken == null || !registrationToken.equals(savedToken)) {
                return false;
            }
            session.removeAttribute(SessionKeys.ISSUE_CREATE_TOKEN);
            return true;
        }
    }

    private void restoreRegistrationToken(HttpSession session, String registrationToken) {
        synchronized (session) {
            session.setAttribute(SessionKeys.ISSUE_CREATE_TOKEN, registrationToken);
        }
    }
}
