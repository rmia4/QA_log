package egovframework.main;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import egovframework.common.SessionKeys;
import egovframework.issue.dto.IssueListItemDTO;
import egovframework.issue.service.IssueListService;
import egovframework.main.controller.MainController;
import egovframework.project.gubun.ProjectStatus;
import egovframework.project.service.ProjectService;
import egovframework.project.vo.ProjectVO;

public class MainControllerTest {

    private MockMvc mockMvc;
    private List<IssueListItemDTO> activeIssues;
    private List<IssueListItemDTO> closedIssues;

    @Before
    public void setUp() {
        IssueListItemDTO activeIssue = new IssueListItemDTO();
        activeIssue.setId(202L);
        activeIssue.setTitle("로그인 버튼 오류");
        activeIssues = singletonList(activeIssue);

        IssueListItemDTO closedIssue = new IssueListItemDTO();
        closedIssue.setId(203L);
        closedIssue.setTitle("종료된 오류");
        closedIssues = singletonList(closedIssue);

        MainController controller = new MainController();
        ReflectionTestUtils.setField(controller, "projectService", new ProjectFixturesService());
        ReflectionTestUtils.setField(controller, "issueListService", new IssueListFixturesService());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void anonymousVisitorIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void loggedInVisitorSeesProjectsAndFirstProjectIsSelected() throws Exception {
        mockMvc.perform(get("/").session(loggedInSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("main/main"))
                .andExpect(model().attribute("selectedProjectId", 11L))
                .andExpect(model().attributeExists("projects"))
                .andExpect(request().sessionAttribute(SessionKeys.LOGIN_USER_ID, 7L));
    }

    @Test
    public void requestedProjectIsSelected() throws Exception {
        mockMvc.perform(get("/").param("projectId", "22").session(loggedInSession()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedProjectId", 22L));
    }

    @Test
    public void selectedProjectLoadsActiveIssues() throws Exception {
        mockMvc.perform(get("/").param("projectId", "22").session(loggedInSession()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("listMode", "active"))
                .andExpect(model().attribute("issues", activeIssues));
    }

    @Test
    public void closedTabLoadsClosedIssues() throws Exception {
        mockMvc.perform(get("/")
                .param("projectId", "22")
                .param("view", "closed")
                .session(loggedInSession()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("listMode", "closed"))
                .andExpect(model().attribute("issues", closedIssues));
    }

    @Test
    public void issueListDefaultsToHighestSeverityFirst() throws Exception {
        mockMvc.perform(get("/").param("projectId", "22").session(loggedInSession()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sort", "severity"))
                .andExpect(model().attribute("direction", "desc"));
    }

    @Test
    public void requestedCreatedDateAscendingSortIsKeptInModel() throws Exception {
        mockMvc.perform(get("/")
                .param("projectId", "22")
                .param("sort", "createdAt")
                .param("direction", "asc")
                .session(loggedInSession()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sort", "createdAt"))
                .andExpect(model().attribute("direction", "asc"));
    }

    @Test
    public void projectFormsOnlyReceiveActiveStatusOptions() throws Exception {
        mockMvc.perform(get("/").session(loggedInSession()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("projectStatusOptions", asList(
                        ProjectStatus.IN_PROGRESS, ProjectStatus.MAINTENANCE, ProjectStatus.ON_HOLD)));
    }

    @Test
    public void projectListReceivesAllStatusesInDisplayOrder() throws Exception {
        mockMvc.perform(get("/").session(loggedInSession()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("projectListStatuses", asList(
                        ProjectStatus.IN_PROGRESS, ProjectStatus.MAINTENANCE,
                        ProjectStatus.ON_HOLD, ProjectStatus.ARCHIVED)));
    }

    private MockHttpSession loggedInSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.LOGIN_USER_ID, 7L);
        return session;
    }

    private static final class ProjectFixturesService implements ProjectService {
        @Override
        public List<ProjectVO> getProjectList() {
            ProjectVO first = new ProjectVO();
            first.setId(11L);
            first.setName("첫 번째 프로젝트");
            first.setOpenIssueCount(2L);

            ProjectVO second = new ProjectVO();
            second.setId(22L);
            second.setName("두 번째 프로젝트");
            second.setOpenIssueCount(0L);
            return asList(first, second);
        }

        @Override
        public ProjectVO getProject(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ProjectVO createProject(String name, String status, Long createdBy) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updateProject(Long id, String name, String status) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean archiveProject(Long id) {
            throw new UnsupportedOperationException();
        }
    }

    private final class IssueListFixturesService implements IssueListService {
        @Override
        public List<IssueListItemDTO> getIssueList(Long projectId, boolean closed, String sort, String direction) {
            if (!Long.valueOf(22L).equals(projectId)) {
                return java.util.Collections.emptyList();
            }
            return closed ? closedIssues : activeIssues;
        }
    }
}
