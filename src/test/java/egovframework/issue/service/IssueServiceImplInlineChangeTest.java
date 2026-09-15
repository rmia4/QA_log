package egovframework.issue.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import egovframework.issue.dto.IssueDetailResponseDTO;
import egovframework.issue.dto.IssueInlineChangeResponseDTO;
import egovframework.issue.exception.InvalidIssueValueException;
import egovframework.issue.exception.IssueConflictException;
import egovframework.issue.exception.IssueForbiddenException;
import egovframework.issue.mapper.IssueAttachmentMapper;
import egovframework.issue.mapper.IssueHistoryMapper;
import egovframework.issue.mapper.IssueMapper;
import egovframework.issue.service.impl.IssueServiceImpl;
import egovframework.issue.vo.IssueHistoryVO;
import egovframework.project.mapper.ProjectMapper;
import egovframework.user.mapper.UserMapper;

public class IssueServiceImplInlineChangeTest {

    private static final LocalDateTime EXPECTED = LocalDateTime.of(2026, 9, 15, 10, 0);
    private static final LocalDateTime UPDATED = LocalDateTime.of(2026, 9, 15, 10, 1, 2, 123456000);

    private IssueServiceImpl issueService;
    private IssueDetailResponseDTO issue;
    private boolean projectAssignee;
    private int affectedRows;
    private int updateCalls;
    private final List<IssueHistoryVO> histories = new ArrayList<>();

    @Before
    public void setUp() {
        issue = new IssueDetailResponseDTO();
        issue.setId(8L);
        issue.setProjectId(22L);
        issue.setCreatedBy(7L);
        issue.setAssigneeId(9L);
        issue.setAssigneeName("기존 담당자");
        issue.setStatus("new");
        issue.setSeverity("medium");
        issue.setPriority("normal");
        issue.setUpdatedAt(EXPECTED);
        projectAssignee = false;
        affectedRows = 1;
        updateCalls = 0;
        histories.clear();

        IssueMapper issueMapper = proxy(IssueMapper.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                String name = method.getName();
                if ("selectIssueDetail".equals(name)) {
                    return issue;
                }
                if ("selectIssueUpdatedAt".equals(name)) {
                    return UPDATED;
                }
                if ("updateSeverity".equals(name) || "updatePriority".equals(name)
                        || "updateStatus".equals(name) || "updateAssignee".equals(name)) {
                    updateCalls++;
                    return affectedRows;
                }
                return defaultValue(method.getReturnType());
            }
        });
        IssueHistoryMapper historyMapper = proxy(IssueHistoryMapper.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("insertHistory".equals(method.getName())) {
                    histories.add((IssueHistoryVO) args[0]);
                }
                return defaultValue(method.getReturnType());
            }
        });
        IssueAttachmentMapper attachmentMapper = proxy(IssueAttachmentMapper.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("selectAttachments".equals(method.getName())) {
                    return Collections.emptyList();
                }
                return defaultValue(method.getReturnType());
            }
        });
        AttachmentStorageService storageService = proxy(AttachmentStorageService.class, emptyHandler());
        UserMapper userMapper = proxy(UserMapper.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("selectDisplayName".equals(method.getName())) {
                    return "새 담당자";
                }
                return defaultValue(method.getReturnType());
            }
        });
        ProjectMapper projectMapper = proxy(ProjectMapper.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("existsProjectAssignee".equals(method.getName())) {
                    return projectAssignee;
                }
                return defaultValue(method.getReturnType());
            }
        });

        issueService = new IssueServiceImpl(
                issueMapper, historyMapper, attachmentMapper, storageService, userMapper, projectMapper);
    }

    @Test
    public void projectAssigneeCanChangeSeverityAndReceivesNewTimestamp() {
        projectAssignee = true;

        IssueInlineChangeResponseDTO result = issueService.changeSeverity(8L, "critical", EXPECTED, 11L);

        assertEquals("critical", result.getValue());
        assertEquals("치명적", result.getLabel());
        assertEquals(UPDATED.toString(), result.getUpdatedAt());
        assertEquals("2026-09-15 19:01", result.getUpdatedAtDisplay());
        assertEquals(1, histories.size());
        assertEquals("severity", histories.get(0).getFieldName());
        assertEquals("보통", histories.get(0).getOldValue());
        assertEquals("치명적", histories.get(0).getNewValue());
    }

    @Test
    public void priorityCanBeChangedToUnspecified() {
        IssueInlineChangeResponseDTO result = issueService.changePriority(8L, "unspecified", EXPECTED, 7L);

        assertEquals("unspecified", result.getValue());
        assertEquals("미지정", result.getLabel());
        assertEquals("priority", histories.get(0).getFieldName());
        assertEquals("보통", histories.get(0).getOldValue());
        assertEquals("미지정", histories.get(0).getNewValue());
    }

    @Test(expected = InvalidIssueValueException.class)
    public void unknownSeverityIsRejectedBeforeUpdate() {
        issueService.changeSeverity(8L, "blocker", EXPECTED, 7L);
    }

    @Test
    public void staleSeverityChangeRaisesConflictWithoutHistory() {
        affectedRows = 0;
        try {
            issueService.changeSeverity(8L, "high", EXPECTED, 7L);
        } catch (IssueConflictException expected) {
            assertEquals(0, histories.size());
            return;
        }
        throw new AssertionError("IssueConflictException이 발생해야 합니다.");
    }

    @Test(expected = IssueForbiddenException.class)
    public void unrelatedUserCannotChangePriority() {
        issueService.changePriority(8L, "high", EXPECTED, 11L);
    }

    @Test
    public void projectAssigneeCanChangeStatus() {
        projectAssignee = true;

        IssueInlineChangeResponseDTO result = issueService.changeStatus(8L, "closed", EXPECTED, 11L);

        assertEquals("closed", result.getValue());
        assertEquals("종료", result.getLabel());
        assertEquals("status", histories.get(0).getFieldName());
    }

    @Test
    public void projectAssigneeCanClearAssignee() {
        projectAssignee = true;

        IssueInlineChangeResponseDTO result = issueService.changeAssignee(8L, null, EXPECTED, 11L);

        assertEquals("", result.getValue());
        assertEquals("미지정", result.getLabel());
        assertEquals("assignee_id", histories.get(0).getFieldName());
    }

    @Test
    public void managementPermissionIncludesCreatorIssueAssigneeAndProjectAssignee() {
        assertTrue(issueService.canManageIssue(8L, 7L));
        assertTrue(issueService.canManageIssue(8L, 9L));
        projectAssignee = true;
        assertTrue(issueService.canManageIssue(8L, 11L));
    }

    private static InvocationHandler emptyHandler() {
        return new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                return defaultValue(method.getReturnType());
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        return null;
    }
}
