package egovframework.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import egovframework.project.vo.ProjectVO;

public interface ProjectMapper {

    List<ProjectVO> selectProjectList();

    ProjectVO selectProject(Long id);

    void insertProject(ProjectVO project);

    void updateProject(@Param("id") Long id, @Param("name") String name,
            @Param("status") String status);

    int archiveProject(@Param("id") Long id);
}
