package egovframework.issue.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import egovframework.common.SessionKeys;
import egovframework.issue.dto.IssueInlineChangeResponseDTO;
import egovframework.issue.service.IssueService;

public class IssueControllerInlineChangeTest {

    private static final LocalDateTime EXPECTED = LocalDateTime.of(2026, 9, 15, 10, 0);
    private static final LocalDateTime UPDATED = LocalDateTime.of(2026, 9, 15, 10, 1);

    private MockMvc mockMvc;
    private String calledMethod;
    private Object changedValue;
    private Long actorId;

    @Before
    public void setUp() {
        IssueController controller = new IssueController();
        IssueService service = proxy(IssueService.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                calledMethod = method.getName();
                changedValue = args[1];
                actorId = (Long) args[3];
                String value = args[1] == null ? "" : args[1].toString();
                String label = value.isEmpty() ? "미지정" : "변경값";
                return new IssueInlineChangeResponseDTO(value, label, UPDATED);
            }
        });
        ReflectionTestUtils.setField(controller, "issueService", service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void severityCanBeChangedWithFormEncodedRequest() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/issues/8/severity")
                .param("severity", "critical")
                .param("expectedUpdatedAt", EXPECTED.toString())
                .session(loggedInSession()))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("\"value\":\"critical\""));
        assertTrue(response.contains("\"label\":\"변경값\""));
        assertTrue(response.contains("\"updatedAt\":\"" + UPDATED.toString() + "\""));

        assertEquals("changeSeverity", calledMethod);
        assertEquals("critical", changedValue);
        assertEquals(Long.valueOf(7L), actorId);
    }

    @Test
    public void blankAssigneeIsPassedAsNull() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/issues/8/assignee")
                .param("assigneeId", "")
                .param("expectedUpdatedAt", EXPECTED.toString())
                .session(loggedInSession()))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("\"value\":\"\""));
        assertTrue(response.contains("\"label\":\"미지정\""));

        assertEquals("changeAssignee", calledMethod);
        assertEquals(null, changedValue);
    }

    private MockHttpSession loggedInSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.LOGIN_USER_ID, 7L);
        return session;
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler);
    }
}
