package egovframework.issue;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.io.IOException;
import java.util.List;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import egovframework.common.SessionKeys;
import egovframework.issue.controller.IssueRegisterViewController;
import egovframework.issue.dto.IssueDetailResponseDTO;
import egovframework.issue.dto.IssueHistoryResponseDTO;
import egovframework.issue.dto.request.IssueSaveRequestDTO;
import egovframework.issue.service.IssueService;
import egovframework.project.service.ProjectService;
import egovframework.project.vo.ProjectVO;
import egovframework.user.mapper.UserMapper;
import egovframework.user.vo.UserVO;

public class IssueRegisterViewControllerTest {

    private MockMvc mockMvc;
    private RecordingIssueService issueService;

    @Before
    public void setUp() {
        IssueRegisterViewController controller = new IssueRegisterViewController();
        issueService = new RecordingIssueService();
        ReflectionTestUtils.setField(controller, "issueService", issueService);
        ReflectionTestUtils.setField(controller, "projectService", new ProjectFixturesService());
        ReflectionTestUtils.setField(controller, "userMapper", new UserFixturesMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void anonymousVisitorIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/projects/22/issues/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void formKeepsProjectNavigationAndSelectedProject() throws Exception {
        mockMvc.perform(get("/projects/22/issues/new").session(loggedInSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("issue/register"))
                .andExpect(model().attributeExists("projects", "project", "users"))
                .andExpect(model().attribute("selectedProjectId", 22L));
    }

    @Test
    public void sameRegistrationTokenCreatesOnlyOneIssue() throws Exception {
        MockHttpSession session = loggedInSession();
        session.setAttribute("ISSUE_CREATE_TOKEN", "one-time-token");

        mockMvc.perform(post("/projects/22/issues")
                .param("registrationToken", "one-time-token")
                .param("title", "중복되면 안 되는 오류")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/issues/301"));

        mockMvc.perform(post("/projects/22/issues")
                .param("registrationToken", "one-time-token")
                .param("title", "중복되면 안 되는 오류")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?projectId=22&issueCreateError=duplicate"));

        assertEquals(1, issueService.createCount);
    }

    private MockHttpSession loggedInSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.LOGIN_USER_ID, 7L);
        return session;
    }

    private static final class ProjectFixturesService implements ProjectService {
        @Override
        public List<ProjectVO> getProjectList() {
            return asList(project(11L, "첫 번째"), project(22L, "두 번째"));
        }

        @Override
        public ProjectVO getProject(Long id) {
            return project(id, "두 번째");
        }

        private ProjectVO project(Long id, String name) {
            ProjectVO project = new ProjectVO();
            project.setId(id);
            project.setName(name);
            return project;
        }

        @Override public ProjectVO createProject(String name, Long createdBy) { throw new UnsupportedOperationException(); }
        @Override public void updateProject(Long id, String name) { throw new UnsupportedOperationException(); }
        @Override public boolean deleteProject(Long id) { throw new UnsupportedOperationException(); }
    }

    private static final class UserFixturesMapper implements UserMapper {
        @Override
        public List<UserVO> selectAllForOptions() {
            return java.util.Collections.emptyList();
        }

        @Override public UserVO selectByLoginId(String loginId) { throw new UnsupportedOperationException(); }
        @Override public void insertUser(UserVO user) { throw new UnsupportedOperationException(); }
        @Override public String selectDisplayName(Long id) { throw new UnsupportedOperationException(); }
    }

    private static final class RecordingIssueService implements IssueService {
        private int createCount;

        @Override
        public Long createIssue(Long projectId, IssueSaveRequestDTO request,
                List<MultipartFile> files, List<MultipartFile> expectedResultFiles,
                List<MultipartFile> actualResultFiles, Long actorId) {
            createCount++;
            return 301L;
        }

        @Override public IssueDetailResponseDTO getIssueDetail(Long id) { throw new UnsupportedOperationException(); }
        @Override
        public void updateIssueFields(Long id, IssueSaveRequestDTO request,
                List<MultipartFile> files, List<MultipartFile> expectedResultFiles, List<MultipartFile> actualResultFiles,
                List<Long> attachmentIdsToDelete, LocalDateTime expectedUpdatedAt, Long actorId) throws IOException {
            throw new UnsupportedOperationException();
        }
        @Override public void changeAssignee(Long id, Long assigneeId, LocalDateTime expectedUpdatedAt, Long actorId) { throw new UnsupportedOperationException(); }
        @Override public void changeStatus(Long id, String status, LocalDateTime expectedUpdatedAt, Long actorId) { throw new UnsupportedOperationException(); }
        @Override public void closeIssue(Long id, LocalDateTime expectedUpdatedAt, Long actorId) { throw new UnsupportedOperationException(); }
        @Override public void reopenIssue(Long id, LocalDateTime expectedUpdatedAt, Long actorId) { throw new UnsupportedOperationException(); }
        @Override public List<IssueHistoryResponseDTO> getHistories(Long id) { throw new UnsupportedOperationException(); }
    }
}
