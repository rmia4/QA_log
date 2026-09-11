package egovframework.login.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import egovframework.common.SessionKeys;
import egovframework.login.service.LoginService;
import egovframework.user.vo.UserVO;

@Controller
public class LoginController {

    private static final String LOGIN_ERROR = "아이디 또는 비밀번호가 올바르지 않습니다.";

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("login_id") String loginId,
                        @RequestParam("password") String password,
                        HttpServletRequest request,
                        Model model) {
        UserVO user = loginService.authenticate(loginId, password);
        if (user == null) {
            model.addAttribute("loginId", loginId == null ? "" : loginId.trim());
            model.addAttribute("loginError", LOGIN_ERROR);
            return "login/login";
        }

        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(SessionKeys.LOGIN_SESSION_SECONDS);
        session.setAttribute(SessionKeys.LOGIN_USER_ID, user.getId());
        session.setAttribute("loginDisplayName", user.getDisplayName());
        return "redirect:/";
    }
}
