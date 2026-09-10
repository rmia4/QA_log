package egovframework.project.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        project.setName(name);
        project.setCreatedBy(createdBy);
        projectMapper.insertProject(project);
        return projectMapper.selectProject(project.getId());
    }
}
