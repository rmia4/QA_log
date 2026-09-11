package egovframework.login;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import egovframework.common.SessionKeys;
import egovframework.login.controller.LoginController;
import egovframework.login.service.LoginService;
import egovframework.user.mapper.UserMapper;
import egovframework.user.vo.UserVO;

public class LoginControllerTest {

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        UserVO user = new UserVO();
        user.setId(7L);
        user.setLoginId("tester");
        user.setDisplayName("테스터");
        user.setPasswordHash(new BCryptPasswordEncoder().encode("correct-password"));

        LoginService loginService = new LoginService(new StubUserMapper(user));
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");
        mockMvc = MockMvcBuilders.standaloneSetup(new LoginController(loginService))
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    public void loginPageIsAvailable() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login/login"));
    }

    @Test
    public void validCredentialsCreateLoginSession() throws Exception {
        mockMvc.perform(post("/login")
                .param("login_id", "  tester  ")
                .param("password", "correct-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(request().sessionAttribute(SessionKeys.LOGIN_USER_ID, 7L))
                .andExpect(request().sessionAttribute("loginDisplayName", "테스터"));
    }

    @Test
    public void invalidCredentialsReturnGenericError() throws Exception {
        mockMvc.perform(post("/login")
                .param("login_id", "tester")
                .param("password", "wrong-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("login/login"))
                .andExpect(model().attribute("loginId", "tester"))
                .andExpect(model().attribute("loginError", "아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    private static final class StubUserMapper implements UserMapper {
        private final UserVO user;

        private StubUserMapper(UserVO user) {
            this.user = user;
        }

        @Override
        public UserVO selectByLoginId(String loginId) {
            return user.getLoginId().equals(loginId) ? user : null;
        }

        @Override
        public String selectDisplayName(Long id) {
            return user.getId().equals(id) ? user.getDisplayName() : null;
        }

        @Override
        public java.util.List<UserVO> selectAllForOptions() {
            return java.util.Collections.singletonList(user);
        }
    }
}
