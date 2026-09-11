package egovframework.project.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import egovframework.common.SessionKeys;
import egovframework.issue.service.IssueListService;
import egovframework.project.service.ProjectService;
import egovframework.project.vo.ProjectVO;

/**
 * 스캐폴딩 배선 확인용 - Controller -> Service -> Mapper -> DB까지 한 번에 도는지 증명하는 최소 예시.
 * 실제 메인 화면(좌측 프로젝트 목록 UI)은 별도로 설계해서 대체한다.
 */
@Controller
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private IssueListService issueListService;

    @GetMapping("/")
    public String main(@RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "view", required = false, defaultValue = "active") String view,
            Model model, HttpSession session) {
        if (session.getAttribute(SessionKeys.LOGIN_USER_ID) == null) {
            return "redirect:/login";
        }
        List<ProjectVO> projects = projectService.getProjectList();
        model.addAttribute("projects", projects);
        String listMode = "closed".equals(view) ? "closed" : "active";
        model.addAttribute("listMode", listMode);
        if (!projects.isEmpty()) {
            ProjectVO selectedProject = projects.get(0);
            if (projectId != null) {
                for (ProjectVO project : projects) {
                    if (projectId.equals(project.getId())) {
                        selectedProject = project;
                        break;
                    }
                }
            }
            model.addAttribute("selectedProjectId", selectedProject.getId());
            model.addAttribute("selectedProject", selectedProject);
            model.addAttribute("issues", issueListService.getIssueList(
                    selectedProject.getId(), "closed".equals(listMode)));
        }
        return "main/main";
    }

    @GetMapping("/api/projects")
    @ResponseBody
    public List<ProjectVO> list() {
        return projectService.getProjectList();
    }
}
