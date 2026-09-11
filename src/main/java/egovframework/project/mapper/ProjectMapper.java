package egovframework.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import egovframework.project.vo.ProjectVO;

public interface ProjectMapper {

    List<ProjectVO> selectProjectList();

    ProjectVO selectProject(Long id);

    void insertProject(ProjectVO project);

    void updateProject(@Param("id") Long id, @Param("name") String name);

    int countIssuesByProjectId(@Param("projectId") Long projectId);

    void deleteProject(@Param("id") Long id);
}
