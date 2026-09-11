package egovframework.signup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import egovframework.common.SessionKeys;
import egovframework.signup.controller.SignupController;
import egovframework.signup.service.SignupService;
import egovframework.team.mapper.TeamSettingsMapper;
import egovframework.user.mapper.UserMapper;
import egovframework.user.vo.UserVO;

public class SignupControllerTest {

    private MockMvc mockMvc;
    private RecordingUserMapper userMapper;

    @Before
    public void setUp() {
        userMapper = new RecordingUserMapper();
        String inviteCodeHash = new BCryptPasswordEncoder().encode("TEAM-2026");
        SignupService signupService = new SignupService(userMapper, new FixedTeamSettingsMapper(inviteCodeHash));
        mockMvc = MockMvcBuilders.standaloneSetup(new SignupController(signupService)).build();
    }

    @Test
    public void signupPageIsAvailable() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup/signup"));
    }

    @Test
    public void validSignupCreatesUserAndLoginSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/signup")
                .param("invite_code", "TEAM-2026")
                .param("login_id", "new-user")
                .param("password", "simple-password")
                .param("display_name", "새 사용자"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttribute(SessionKeys.LOGIN_USER_ID, 41L))
                .andExpect(request().sessionAttribute("loginDisplayName", "새 사용자"))
                .andReturn();

        assertEquals(2592000, result.getRequest().getSession(false).getMaxInactiveInterval());
        assertEquals("new-user", userMapper.insertedUser.getLoginId());
        assertEquals("새 사용자", userMapper.insertedUser.getDisplayName());
        assertTrue(new BCryptPasswordEncoder().matches(
                "simple-password", userMapper.insertedUser.getPasswordHash()));
    }

    @Test
    public void invalidInviteCodeReturnsSignupError() throws Exception {
        mockMvc.perform(post("/signup")
                .param("invite_code", "WRONG")
                .param("login_id", "new-user")
                .param("password", "simple-password")
                .param("display_name", "새 사용자"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup/signup"))
                .andExpect(model().attribute("signupError", "초대코드가 올바르지 않습니다."));
    }

    @Test
    public void duplicateLoginIdReturnsSignupError() throws Exception {
        userMapper.existingUser = new UserVO();

        mockMvc.perform(post("/signup")
                .param("invite_code", "TEAM-2026")
                .param("login_id", "existing")
                .param("password", "simple-password")
                .param("display_name", "기존 사용자"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup/signup"))
                .andExpect(model().attribute("signupError", "이미 사용 중인 아이디입니다."));
    }

    private static final class FixedTeamSettingsMapper implements TeamSettingsMapper {
        private final String inviteCodeHash;

        private FixedTeamSettingsMapper(String inviteCodeHash) {
            this.inviteCodeHash = inviteCodeHash;
        }

        @Override
        public String selectInviteCodeHash() {
            return inviteCodeHash;
        }
    }

    private static final class RecordingUserMapper implements UserMapper {
        private UserVO existingUser;
        private UserVO insertedUser;

        @Override
        public UserVO selectByLoginId(String loginId) {
            return existingUser;
        }

        @Override
        public void insertUser(UserVO user) {
            insertedUser = user;
            user.setId(41L);
        }

        @Override
        public String selectDisplayName(Long id) {
            return null;
        }

        @Override
        public List<UserVO> selectAllForOptions() {
            return Collections.emptyList();
        }
    }
}
