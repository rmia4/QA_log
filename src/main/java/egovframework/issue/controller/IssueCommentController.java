package egovframework.issue.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import egovframework.common.SessionKeys;
import egovframework.issue.dto.IssueCommentResponseDTO;
import egovframework.issue.service.IssueCommentService;

@RestController
@RequestMapping("/api")
public class IssueCommentController {

    @Autowired
    private IssueCommentService issueCommentService;

    @RequestMapping(value = "/issues/{id}/comments", method = RequestMethod.GET)
    public List<IssueCommentResponseDTO> list(@PathVariable Long id) {
        return issueCommentService.getComments(id);
    }

    /**
     * multipart/form-data로 받는다(스크린샷 첨부, 2026-09-14 추가) - JSON 바디는 파일과 함께 보낼 수
     * 없어서 IssueController.create()와 동일하게 개별 폼 필드로 받는 방식으로 바꿨다.
     */
    @RequestMapping(value = "/issues/{id}/comments", method = RequestMethod.POST)
    public Long add(@PathVariable Long id, @RequestParam String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            HttpSession session) throws IOException {
        Long actorId = (Long) session.getAttribute(SessionKeys.LOGIN_USER_ID);
        return issueCommentService.addComment(id, content, files, actorId);
    }
}
