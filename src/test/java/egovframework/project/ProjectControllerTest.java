package egovframework.project;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import egovframework.common.SessionKeys;
import egovframework.project.controller.ProjectController;
import egovframework.project.service.ProjectService;
import egovframework.project.vo.ProjectVO;

public class ProjectControllerTest {

    private MockMvc mockMvc;
    private RecordingProjectService projectService;

    @Before
    public void setUp() {
        projectService = new RecordingProjectService();
        ProjectController controller = new ProjectController();
        ReflectionTestUtils.setField(controller, "projectService", projectService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void anonymousVisitorCannotCreateProject() throws Exception {
        mockMvc.perform(post("/projects").param("name", "새 프로젝트").param("assigneeIds", "7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void loggedInVisitorCreatesProjectWithMultipleAssignees() throws Exception {
        mockMvc.perform(post("/projects")
                .param("name", "새 프로젝트")
                .param("status", "maintenance")
                .param("assigneeIds", "7", "9")
                .session(loggedInSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?projectId=31"));

        assertEquals("새 프로젝트", projectService.createdName);
        assertEquals("maintenance", projectService.createdStatus);
        assertEquals(Long.valueOf(7L), projectService.createdBy);
        assertEquals(java.util.Arrays.asList(7L, 9L), projectService.createdAssigneeIds);
    }

    @Test
    public void loggedInVisitorUpdatesProjectWithMultipleAssignees() throws Exception {
        mockMvc.perform(post("/projects/22/edit")
                .param("name", "수정한 프로젝트")
                .param("status", "in_progress")
                .param("assigneeIds", "7", "9")
                .session(loggedInSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?projectId=22"));

        assertEquals(Long.valueOf(22L), projectService.updatedId);
        assertEquals("수정한 프로젝트", projectService.updatedName);
        assertEquals("in_progress", projectService.updatedStatus);
        assertEquals(java.util.Arrays.asList(7L, 9L), projectService.updatedAssigneeIds);
        assertEquals(Long.valueOf(7L), projectService.updatedBy);
    }

    @Test
    public void projectWithOnlyClosedIssuesCanBeArchived() throws Exception {
        projectService.archivable = true;

        mockMvc.perform(post("/projects/22/archive").session(loggedInSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertEquals(Long.valueOf(22L), projectService.archivedId);
        assertEquals(Long.valueOf(7L), projectService.archivedBy);
    }

    @Test
    public void projectWithOpenIssuesIsKeptAndShowsReason() throws Exception {
        projectService.archivable = false;

        mockMvc.perform(post("/projects/22/archive").session(loggedInSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?projectId=22&projectArchiveError=hasOpenIssues"));

        assertEquals(Long.valueOf(22L), projectService.archivedId);
    }

    private MockHttpSession loggedInSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.LOGIN_USER_ID, 7L);
        return session;
    }

    private static final class RecordingProjectService implements ProjectService {
        private String createdName;
        private String createdStatus;
        private Long createdBy;
        private List<Long> createdAssigneeIds;
        private Long updatedId;
        private String updatedName;
        private String updatedStatus;
        private List<Long> updatedAssigneeIds;
        private Long updatedBy;
        private Long archivedId;
        private Long archivedBy;
        private boolean archivable;

        @Override
        public List<ProjectVO> getProjectList() {
            return emptyList();
        }

        @Override
        public ProjectVO getProject(Long id) {
            return null;
        }

        @Override
        public ProjectVO createProject(String name, String status, Long createdBy, List<Long> assigneeIds) {
            this.createdName = name;
            this.createdStatus = status;
            this.createdBy = createdBy;
            this.createdAssigneeIds = new ArrayList<>(assigneeIds);
            ProjectVO project = new ProjectVO();
            project.setId(31L);
            return project;
        }

        @Override
        public void updateProject(Long id, String name, String status, List<Long> assigneeIds, Long actorId) {
            updatedId = id;
            updatedName = name;
            updatedStatus = status;
            updatedAssigneeIds = new ArrayList<>(assigneeIds);
            updatedBy = actorId;
        }

        @Override
        public boolean archiveProject(Long id, Long actorId) {
            archivedId = id;
            archivedBy = actorId;
            return archivable;
        }

        @Override
        public boolean isProjectAssignee(Long projectId, Long userId) {
            return true;
        }
    }
}
