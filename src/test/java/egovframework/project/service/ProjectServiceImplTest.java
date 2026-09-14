package egovframework.project.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import egovframework.project.exception.ProjectForbiddenException;
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
        ProjectVO created = projectService.createProject(
                "새 프로젝트", "maintenance", 7L, singletonList(7L));

        assertEquals("새 프로젝트", created.getName());
        assertEquals("maintenance", created.getStatus());
    }

    @Test
    public void blankStatusDefaultsNewProjectToInProgress() {
        ProjectVO created = projectService.createProject(
                "새 프로젝트", "", 7L, singletonList(7L));

        assertEquals("in_progress", created.getStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void archivedStatusCannotBeSelectedDuringCreate() {
        projectService.createProject("새 프로젝트", "archived", 7L, singletonList(7L));
    }

    @Test
    public void projectNameStatusAndAssigneesAreUpdated() {
        projectMapper.assigneeIds.add(7L);

        projectService.updateProject(
                22L, "  QA 프로젝트  ", "in_progress", asList(7L, 9L), 7L);

        assertEquals("QA 프로젝트", projectMapper.project.getName());
        assertEquals("in_progress", projectMapper.project.getStatus());
        assertEquals(asList(7L, 9L), projectMapper.assigneeIds);
    }

    @Test
    public void blankStatusKeepsArchivedProjectArchived() {
        projectMapper.project.setStatus("archived");
        projectMapper.assigneeIds.add(7L);

        projectService.updateProject(
                22L, "이름만 변경", "", singletonList(7L), 7L);

        assertEquals("이름만 변경", projectMapper.project.getName());
        assertEquals("archived", projectMapper.project.getStatus());
    }

    @Test(expected = ProjectForbiddenException.class)
    public void unassignedUserCannotUpdateProject() {
        projectMapper.assigneeIds.add(9L);

        projectService.updateProject(
                22L, "수정 시도", "in_progress", singletonList(9L), 7L);
    }

    @Test
    public void projectWithOpenIssuesCannotBeArchived() {
        projectMapper.openIssueCount = 1;
        projectMapper.assigneeIds.add(7L);

        assertFalse(projectService.archiveProject(22L, 7L));
        assertEquals("on_hold", projectMapper.project.getStatus());
    }

    @Test
    public void projectWithOnlyClosedIssuesIsArchived() {
        projectMapper.openIssueCount = 0;
        projectMapper.assigneeIds.add(7L);

        assertTrue(projectService.archiveProject(22L, 7L));
        assertEquals("archived", projectMapper.project.getStatus());
    }

    @Test(expected = ProjectForbiddenException.class)
    public void unassignedUserCannotArchiveProject() {
        projectMapper.assigneeIds.add(9L);

        projectService.archiveProject(22L, 7L);
    }

    @Test
    public void projectStoresMultipleUniqueAssigneesDuringCreate() {
        ProjectVO created = projectService.createProject(
                "새 프로젝트", "in_progress", 7L, asList(7L, 9L, 7L));

        assertEquals(asList(7L, 9L), created.getAssigneeIds());
    }

    @Test(expected = IllegalArgumentException.class)
    public void projectRequiresAtLeastOneAssignee() {
        projectService.createProject(
                "새 프로젝트", "in_progress", 7L, Collections.<Long>emptyList());
    }

    @Test
    public void assignedUserCanManageProject() {
        projectMapper.assigneeIds.addAll(asList(7L, 9L));

        assertTrue(projectService.isProjectAssignee(22L, 9L));
        assertFalse(projectService.isProjectAssignee(22L, 5L));
    }

    private static final class RecordingProjectMapper implements ProjectMapper {
        private int openIssueCount;
        private final ProjectVO project = new ProjectVO();
        private final List<Long> assigneeIds = new ArrayList<>();

        private RecordingProjectMapper() {
            project.setId(22L);
            project.setName("기존 프로젝트");
            project.setStatus("on_hold");
        }

        @Override
        public List<ProjectVO> selectProjectList() {
            return singletonList(project);
        }

        @Override
        public ProjectVO selectProject(Long id) {
            project.setAssigneeIds(new ArrayList<>(assigneeIds));
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

        @Override
        public List<Long> selectProjectAssigneeIds(Long projectId) {
            return new ArrayList<>(assigneeIds);
        }

        @Override
        public void insertProjectAssignee(Long projectId, Long userId) {
            assigneeIds.add(userId);
        }

        @Override
        public void deleteProjectAssignees(Long projectId) {
            assigneeIds.clear();
        }

        @Override
        public boolean existsProjectAssignee(Long projectId, Long userId) {
            return assigneeIds.contains(userId);
        }
    }
}
