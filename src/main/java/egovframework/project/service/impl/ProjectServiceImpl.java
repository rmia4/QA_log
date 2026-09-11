package egovframework.project.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ProjectVO createProject(String name, Long createdBy) {
        ProjectVO project = new ProjectVO();
        project.setName(normalizeName(name));
        project.setCreatedBy(createdBy);
        projectMapper.insertProject(project);
        return projectMapper.selectProject(project.getId());
    }

    @Override
    public void updateProject(Long id, String name) {
        projectMapper.updateProject(id, normalizeName(name));
    }

    @Override
    @Transactional
    public boolean deleteProject(Long id) {
        if (projectMapper.countIssuesByProjectId(id) > 0) {
            return false;
        }
        projectMapper.deleteProject(id);
        return true;
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
}
