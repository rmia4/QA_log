package egovframework.project.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.project.exception.ProjectForbiddenException;
import egovframework.project.gubun.ProjectStatus;
import egovframework.project.mapper.ProjectMapper;
import egovframework.project.service.ProjectService;
import egovframework.project.vo.ProjectVO;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public List<ProjectVO> getProjectList() {
        return projectMapper.selectProjectList();
    }

    @Override
    public ProjectVO getProject(Long id) {
        return projectMapper.selectProject(id);
    }

    @Override
    @Transactional
    public ProjectVO createProject(String name, String status, Long createdBy, List<Long> assigneeIds) {
        List<Long> normalizedAssigneeIds = normalizeAssigneeIds(assigneeIds);
        ProjectVO project = new ProjectVO();
        project.setName(normalizeName(name));
        project.setStatus(normalizeActiveStatus(status, true));
        project.setCreatedBy(createdBy);
        projectMapper.insertProject(project);
        replaceAssignees(project.getId(), normalizedAssigneeIds);
        return projectMapper.selectProject(project.getId());
    }

    @Override
    @Transactional
    public void updateProject(Long id, String name, String status, List<Long> assigneeIds, Long actorId) {
        requireProjectAssignee(id, actorId);
        List<Long> normalizedAssigneeIds = normalizeAssigneeIds(assigneeIds);
        projectMapper.updateProject(id, normalizeName(name), normalizeActiveStatus(status, false));
        replaceAssignees(id, normalizedAssigneeIds);
    }

    @Override
    @Transactional
    public boolean archiveProject(Long id, Long actorId) {
        requireProjectAssignee(id, actorId);
        return projectMapper.archiveProject(id) > 0;
    }

    @Override
    public boolean isProjectAssignee(Long projectId, Long userId) {
        return projectId != null && userId != null
                && projectMapper.existsProjectAssignee(projectId, userId);
    }

    private void requireProjectAssignee(Long projectId, Long actorId) {
        if (!isProjectAssignee(projectId, actorId)) {
            throw new ProjectForbiddenException(projectId);
        }
    }

    private void replaceAssignees(Long projectId, List<Long> assigneeIds) {
        projectMapper.deleteProjectAssignees(projectId);
        for (Long assigneeId : assigneeIds) {
            projectMapper.insertProjectAssignee(projectId, assigneeId);
        }
    }

    private List<Long> normalizeAssigneeIds(List<Long> assigneeIds) {
        Set<Long> uniqueIds = new LinkedHashSet<>();
        if (assigneeIds != null) {
            for (Long assigneeId : assigneeIds) {
                if (assigneeId != null) {
                    uniqueIds.add(assigneeId);
                }
            }
        }
        if (uniqueIds.isEmpty()) {
            throw new IllegalArgumentException("프로젝트 담당자를 한 명 이상 선택해 주세요.");
        }
        return new ArrayList<>(uniqueIds);
    }

    private String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("프로젝트명을 입력해 주세요.");
        }
        if (normalized.length() > 200) {
            throw new IllegalArgumentException("프로젝트명은 200자 이하여야 합니다.");
        }
        return normalized;
    }

    private String normalizeActiveStatus(String status, boolean useDefault) {
        String normalized = status == null ? "" : status.trim();
        if (normalized.isEmpty()) {
            return useDefault ? ProjectStatus.IN_PROGRESS.getCode() : null;
        }
        ProjectStatus projectStatus = ProjectStatus.fromCode(normalized);
        if (projectStatus == null || projectStatus.isArchived()) {
            throw new IllegalArgumentException("선택할 수 없는 프로젝트 상태입니다.");
        }
        return projectStatus.getCode();
    }
}
