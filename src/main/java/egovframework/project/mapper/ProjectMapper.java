package egovframework.project.mapper;

import java.util.List;

import egovframework.project.vo.ProjectVO;

public interface ProjectMapper {

    List<ProjectVO> selectProjectList();

    ProjectVO selectProject(Long id);

    void insertProject(ProjectVO project);
}
