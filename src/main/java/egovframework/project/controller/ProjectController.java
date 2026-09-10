package egovframework.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @GetMapping("/")
    public String health(Model model) {
        model.addAttribute("projects", projectService.getProjectList());
        return "health";
    }

    @GetMapping("/api/projects")
    @ResponseBody
    public List<ProjectVO> list() {
        return projectService.getProjectList();
    }
}
