package egovframework.project.service;

import java.util.List;

import egovframework.project.vo.ProjectVO;

public interface ProjectService {

    List<ProjectVO> getProjectList();

    ProjectVO getProject(Long id);

    ProjectVO createProject(String name, String status, Long createdBy);

    void updateProject(Long id, String name, String status);

    boolean archiveProject(Long id);
}
