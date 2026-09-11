package egovframework.issue.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import egovframework.common.SessionKeys;
import egovframework.issue.dto.IssueCommentResponseDTO;
import egovframework.issue.dto.request.IssueCommentSaveRequestDTO;
import egovframework.issue.service.IssueCommentService;

@RestController
public class IssueCommentController {

    @Autowired
    private IssueCommentService issueCommentService;

    @RequestMapping(value = "/issues/{id}/comments", method = RequestMethod.GET)
    public List<IssueCommentResponseDTO> list(@PathVariable Long id) {
        return issueCommentService.getComments(id);
    }

    @RequestMapping(value = "/issues/{id}/comments", method = RequestMethod.POST)
    public Long add(@PathVariable Long id, @RequestBody IssueCommentSaveRequestDTO request, HttpSession session) {
        Long actorId = (Long) session.getAttribute(SessionKeys.LOGIN_USER_ID);
        return issueCommentService.addComment(id, request.getContent(), actorId);
    }
}
