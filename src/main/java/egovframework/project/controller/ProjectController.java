package egovframework.project.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import egovframework.common.SessionKeys;
import egovframework.project.service.ProjectService;
import egovframework.project.vo.ProjectVO;

@Controller
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/api/projects")
    @ResponseBody
    public List<ProjectVO> list() {
        return projectService.getProjectList();
    }

    @PostMapping("/projects")
    public String create(@RequestParam("name") String name, HttpSession session) {
        Long userId = loginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        ProjectVO project = projectService.createProject(name, userId);
        return "redirect:/?projectId=" + project.getId();
    }

    @PostMapping("/projects/{id}/edit")
    public String update(@PathVariable("id") Long id, @RequestParam("name") String name,
            HttpSession session) {
        if (loginUserId(session) == null) {
            return "redirect:/login";
        }
        projectService.updateProject(id, name);
        return "redirect:/?projectId=" + id;
    }

    @PostMapping("/projects/{id}/delete")
    public String delete(@PathVariable("id") Long id, HttpSession session) {
        if (loginUserId(session) == null) {
            return "redirect:/login";
        }
        if (!projectService.deleteProject(id)) {
            return "redirect:/?projectId=" + id + "&projectDeleteError=hasIssues";
        }
        return "redirect:/";
    }

    private Long loginUserId(HttpSession session) {
        Object userId = session.getAttribute(SessionKeys.LOGIN_USER_ID);
        return userId instanceof Long ? (Long) userId : null;
    }
}
