package egovframework.common.controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * web.xml의 &lt;error-page&gt;가 전부 이 경로(/error)로 전달한다 - 404/500 등 Tomcat 기본 에러
 * 페이지(스택 트레이스 노출)를 그대로 보여주지 않기 위함(2026-09-14 팀 결정). 상태 코드는 컨테이너가
 * 넣어주는 표준 request attribute(RequestDispatcher.ERROR_STATUS_CODE)에서 읽는다.
 */
@Controller
public class ErrorPageController {

    @RequestMapping("/error")
    public String handle(HttpServletRequest request, Model model) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = (statusAttr instanceof Integer) ? (Integer) statusAttr : 500;

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("message", messageFor(statusCode));
        return "error/error";
    }

    private String messageFor(int statusCode) {
        switch (statusCode) {
            case 400:
                return "잘못된 요청입니다.";
            case 403:
                return "접근 권한이 없습니다.";
            case 404:
                return "요청하신 페이지를 찾을 수 없습니다.";
            case 409:
                return "다른 사용자가 먼저 처리했습니다.";
            default:
                return "서버에 오류가 발생했습니다.";
        }
    }
}
