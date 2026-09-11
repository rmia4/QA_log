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
    public void projectWithIssuesCannotBeDeleted() {
        projectMapper.issueCount = 1;

        assertFalse(projectService.deleteProject(22L));
        assertFalse(projectMapper.deleteCalled);
    }

    @Test
    public void projectWithoutIssuesIsDeleted() {
        projectMapper.issueCount = 0;

        assertTrue(projectService.deleteProject(22L));
        assertTrue(projectMapper.deleteCalled);
        assertEquals(Long.valueOf(22L), projectMapper.deletedId);
    }

    @Test
    public void projectNameIsTrimmedBeforeUpdate() {
        projectService.updateProject(22L, "  QA 프로젝트  ");

        assertEquals("QA 프로젝트", projectMapper.updatedName);
    }

    private static final class RecordingProjectMapper implements ProjectMapper {
        private int issueCount;
        private boolean deleteCalled;
        private Long deletedId;
        private String updatedName;

        @Override
        public List<ProjectVO> selectProjectList() {
            return Collections.emptyList();
        }

        @Override
        public ProjectVO selectProject(Long id) {
            return null;
        }

        @Override
        public void insertProject(ProjectVO project) {
        }

        @Override
        public void updateProject(Long id, String name) {
            updatedName = name;
        }

        @Override
        public int countIssuesByProjectId(Long projectId) {
            return issueCount;
        }

        @Override
        public void deleteProject(Long id) {
            deleteCalled = true;
            deletedId = id;
        }
    }
}
