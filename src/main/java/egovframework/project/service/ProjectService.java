package egovframework.project.service;

import java.util.List;

import egovframework.project.vo.ProjectVO;

public interface ProjectService {

    List<ProjectVO> getProjectList();

    ProjectVO getProject(Long id);

    ProjectVO createProject(String name, String status, Long createdBy, List<Long> assigneeIds);

    void updateProject(Long id, String name, String status, List<Long> assigneeIds, Long actorId);

    boolean archiveProject(Long id, Long actorId);

    boolean isProjectAssignee(Long projectId, Long userId);
}
