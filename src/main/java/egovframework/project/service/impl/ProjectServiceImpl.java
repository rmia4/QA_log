package egovframework.project.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.project.mapper.ProjectMapper;
import egovframework.project.gubun.ProjectStatus;
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
    public ProjectVO createProject(String name, String status, Long createdBy) {
        ProjectVO project = new ProjectVO();
        project.setName(normalizeName(name));
        project.setStatus(normalizeActiveStatus(status, true));
        project.setCreatedBy(createdBy);
        projectMapper.insertProject(project);
        return projectMapper.selectProject(project.getId());
    }

    @Override
    public void updateProject(Long id, String name, String status) {
        projectMapper.updateProject(id, normalizeName(name), normalizeActiveStatus(status, false));
    }

    @Override
    @Transactional
    public boolean archiveProject(Long id) {
        return projectMapper.archiveProject(id) > 0;
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
