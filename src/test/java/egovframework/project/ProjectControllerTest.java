package egovframework.project;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        mockMvc.perform(post("/projects").param("name", "새 프로젝트"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void loggedInVisitorCreatesProject() throws Exception {
        mockMvc.perform(post("/projects").param("name", "새 프로젝트").session(loggedInSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?projectId=31"));

        assertEquals("새 프로젝트", projectService.createdName);
        assertEquals(Long.valueOf(7L), projectService.createdBy);
    }

    @Test
    public void loggedInVisitorUpdatesProject() throws Exception {
        mockMvc.perform(post("/projects/22/edit")
                .param("name", "수정한 프로젝트")
                .session(loggedInSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?projectId=22"));

        assertEquals(Long.valueOf(22L), projectService.updatedId);
        assertEquals("수정한 프로젝트", projectService.updatedName);
    }

    @Test
    public void projectWithoutIssuesCanBeDeleted() throws Exception {
        projectService.deletable = true;

        mockMvc.perform(post("/projects/22/delete").session(loggedInSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertEquals(Long.valueOf(22L), projectService.deletedId);
    }

    @Test
    public void projectWithIssuesIsKeptAndShowsReason() throws Exception {
        projectService.deletable = false;

        mockMvc.perform(post("/projects/22/delete").session(loggedInSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?projectId=22&projectDeleteError=hasIssues"));

        assertEquals(Long.valueOf(22L), projectService.deletedId);
    }

    private MockHttpSession loggedInSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.LOGIN_USER_ID, 7L);
        return session;
    }

    private static final class RecordingProjectService implements ProjectService {
        private String createdName;
        private Long createdBy;
        private Long updatedId;
        private String updatedName;
        private Long deletedId;
        private boolean deletable;

        @Override
        public List<ProjectVO> getProjectList() {
            return emptyList();
        }

        @Override
        public ProjectVO createProject(String name, Long createdBy) {
            this.createdName = name;
            this.createdBy = createdBy;
            ProjectVO project = new ProjectVO();
            project.setId(31L);
            return project;
        }

        @Override
        public void updateProject(Long id, String name) {
            updatedId = id;
            updatedName = name;
        }

        @Override
        public boolean deleteProject(Long id) {
            deletedId = id;
            return deletable;
        }
    }
}
