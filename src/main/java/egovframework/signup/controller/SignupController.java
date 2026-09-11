package egovframework.signup.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import egovframework.common.SessionKeys;
import egovframework.signup.service.SignupService;
import egovframework.signup.service.SignupService.SignupResult;
import egovframework.user.vo.UserVO;

@Controller
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup/signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam("invite_code") String inviteCode,
            @RequestParam("login_id") String loginId,
            @RequestParam("password") String password,
            @RequestParam("display_name") String displayName,
            HttpServletRequest request, Model model) {
        SignupResult result = signupService.register(inviteCode, loginId, password, displayName);
        if (!result.isSuccess()) {
            model.addAttribute("loginId", trim(loginId));
            model.addAttribute("displayName", trim(displayName));
            model.addAttribute("signupError", result.getErrorMessage());
            return "signup/signup";
        }

        UserVO user = result.getUser();
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

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
