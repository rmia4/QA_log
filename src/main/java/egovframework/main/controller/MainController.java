package egovframework.main.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import egovframework.common.SessionKeys;
import egovframework.issue.service.IssueListService;
import egovframework.project.service.ProjectService;
import egovframework.project.gubun.ProjectStatus;
import egovframework.project.vo.ProjectVO;

@Controller
public class MainController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private IssueListService issueListService;

    @GetMapping("/")
    public String main(@RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "view", required = false, defaultValue = "active") String view,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "direction", required = false) String direction,
            Model model, HttpSession session) {
        if (session.getAttribute(SessionKeys.LOGIN_USER_ID) == null) {
            return "redirect:/login";
        }

        List<ProjectVO> projects = projectService.getProjectList();
        model.addAttribute("projects", projects);
        model.addAttribute("projectStatusOptions", ProjectStatus.activeValues());
        String listMode = "closed".equals(view) ? "closed" : "active";
        String sortMode = "createdAt".equals(sort) ? "createdAt" : "severity";
        String sortDirection = "asc".equals(direction) ? "asc" : "desc";
        model.addAttribute("listMode", listMode);
        model.addAttribute("sort", sortMode);
        model.addAttribute("direction", sortDirection);

        if (!projects.isEmpty()) {
            ProjectVO selectedProject = findSelectedProject(projects, projectId);
            model.addAttribute("selectedProjectId", selectedProject.getId());
            model.addAttribute("selectedProject", selectedProject);
            model.addAttribute("issues", issueListService.getIssueList(
                    selectedProject.getId(), "closed".equals(listMode), sortMode, sortDirection));
        }
        return "main/main";
    }

    private ProjectVO findSelectedProject(List<ProjectVO> projects, Long projectId) {
        if (projectId != null) {
            for (ProjectVO project : projects) {
                if (projectId.equals(project.getId())) {
                    return project;
                }
            }
        }
        return projects.get(0);
    }
}
