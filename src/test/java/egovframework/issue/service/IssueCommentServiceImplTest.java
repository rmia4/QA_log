package egovframework.issue.service;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Proxy;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import egovframework.issue.dto.IssueCommentResponseDTO;
import egovframework.issue.exception.IssueForbiddenException;
import egovframework.issue.mapper.IssueCommentMapper;
import egovframework.issue.mapper.IssueHistoryMapper;
import egovframework.issue.mapper.IssueMapper;
import egovframework.issue.service.impl.IssueCommentServiceImpl;
import egovframework.issue.vo.IssueCommentVO;
import egovframework.issue.vo.IssueHistoryVO;
import egovframework.project.service.ProjectService;
import egovframework.project.vo.ProjectVO;

public class IssueCommentServiceImplTest {

    private RecordingCommentMapper commentMapper;
    private AssigneeProjectService projectService;
    private IssueCommentServiceImpl service;

    @Before
    public void setUp() {
        commentMapper = new RecordingCommentMapper();
        projectService = new AssigneeProjectService();
        IssueMapper issueMapper = (IssueMapper) Proxy.newProxyInstance(
                IssueMapper.class.getClassLoader(), new Class<?>[] { IssueMapper.class },
                (proxy, method, args) -> "selectProjectId".equals(method.getName()) ? 22L : null);
        service = new IssueCommentServiceImpl(
                commentMapper, new RecordingHistoryMapper(), issueMapper, projectService);
    }

    @Test
    public void projectAssigneeCanAddComment() {
        projectService.assignedUserId = 7L;

        Long commentId = service.addComment(301L, " 확인했습니다. ", 7L);

        assertEquals(Long.valueOf(41L), commentId);
        assertEquals("확인했습니다.", commentMapper.saved.getContent());
        assertEquals(Long.valueOf(7L), commentMapper.saved.getCreatedBy());
    }

    @Test(expected = IssueForbiddenException.class)
    public void unassignedUserCannotAddComment() {
        projectService.assignedUserId = 9L;

        service.addComment(301L, "댓글", 7L);
    }

    private static final class RecordingCommentMapper implements IssueCommentMapper {
        private IssueCommentVO saved;

        @Override
        public void insertComment(IssueCommentVO comment) {
            comment.setId(41L);
            saved = comment;
        }

        @Override
        public List<IssueCommentResponseDTO> selectComments(Long issueId) {
            return emptyList();
        }
    }

    private static final class RecordingHistoryMapper implements IssueHistoryMapper {
        @Override public void insertHistory(IssueHistoryVO history) { }
        @Override public List<egovframework.issue.dto.IssueHistoryResponseDTO> selectHistories(Long issueId) {
            return emptyList();
        }
    }

    private static final class AssigneeProjectService implements ProjectService {
        private Long assignedUserId;

        @Override
        public boolean isProjectAssignee(Long projectId, Long userId) {
            return Long.valueOf(22L).equals(projectId) && assignedUserId.equals(userId);
        }

        @Override public List<ProjectVO> getProjectList() { return emptyList(); }
        @Override public ProjectVO getProject(Long id) { throw new UnsupportedOperationException(); }
        @Override public ProjectVO createProject(String name, String status, Long createdBy, List<Long> assigneeIds) { throw new UnsupportedOperationException(); }
        @Override public void updateProject(Long id, String name, String status, List<Long> assigneeIds, Long actorId) { throw new UnsupportedOperationException(); }
        @Override public boolean archiveProject(Long id, Long actorId) { throw new UnsupportedOperationException(); }
    }
}
