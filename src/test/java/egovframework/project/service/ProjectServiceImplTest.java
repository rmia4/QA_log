package egovframework.project.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import egovframework.project.mapper.ProjectMapper;
import egovframework.project.service.impl.ProjectServiceImpl;
import egovframework.project.vo.ProjectVO;

public class ProjectServiceImplTest {

    private ProjectServiceImpl projectService;
    private RecordingProjectMapper projectMapper;

    @Before
    public void setUp() {
        projectService = new ProjectServiceImpl();
        projectMapper = new RecordingProjectMapper();
        ReflectionTestUtils.setField(projectService, "projectMapper", projectMapper);
    }

    @Test
    public void projectIsCreatedWithSelectedActiveStatus() {
        ProjectVO created = projectService.createProject("새 프로젝트", "maintenance", 7L);

        assertEquals("새 프로젝트", created.getName());
        assertEquals("maintenance", created.getStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void archivedStatusCannotBeSelectedDuringCreate() {
        projectService.createProject("새 프로젝트", "archived", 7L);
    }

    @Test
    public void projectNameAndActiveStatusAreUpdated() {
        projectService.updateProject(22L, "  QA 프로젝트  ", "in_progress");

        assertEquals("QA 프로젝트", projectMapper.project.getName());
        assertEquals("in_progress", projectMapper.project.getStatus());
    }

    @Test
    public void blankStatusKeepsArchivedProjectArchived() {
        projectMapper.project.setStatus("archived");

        projectService.updateProject(22L, "이름만 변경", "");

        assertEquals("이름만 변경", projectMapper.project.getName());
        assertEquals("archived", projectMapper.project.getStatus());
    }

    @Test
    public void projectWithOpenIssuesCannotBeArchived() {
        projectMapper.openIssueCount = 1;

        assertFalse(projectService.archiveProject(22L));
        assertEquals("working", projectMapper.project.getStatus());
    }

    @Test
    public void projectWithOnlyClosedIssuesIsArchived() {
        projectMapper.openIssueCount = 0;

        assertTrue(projectService.archiveProject(22L));
        assertEquals("archived", projectMapper.project.getStatus());
    }

    private static final class RecordingProjectMapper implements ProjectMapper {
        private int openIssueCount;
        private final ProjectVO project = new ProjectVO();

        private RecordingProjectMapper() {
            project.setId(22L);
            project.setName("기존 프로젝트");
            project.setStatus("working");
        }

        @Override
        public List<ProjectVO> selectProjectList() {
            return Collections.singletonList(project);
        }

        @Override
        public ProjectVO selectProject(Long id) {
            return project;
        }

        @Override
        public void insertProject(ProjectVO newProject) {
            newProject.setId(22L);
            project.setId(newProject.getId());
            project.setName(newProject.getName());
            project.setStatus(newProject.getStatus());
            project.setCreatedBy(newProject.getCreatedBy());
        }

        @Override
        public void updateProject(Long id, String name, String status) {
            project.setName(name);
            if (status != null) {
                project.setStatus(status);
            }
        }

        @Override
        public int archiveProject(Long id) {
            if (openIssueCount > 0) {
                return 0;
            }
            project.setStatus("archived");
            return 1;
        }
    }
}
