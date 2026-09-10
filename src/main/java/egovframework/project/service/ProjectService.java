package egovframework.project.service;

import java.util.List;

import egovframework.project.vo.ProjectVO;

public interface ProjectService {

    List<ProjectVO> getProjectList();

    ProjectVO createProject(String name, Long createdBy);
}
