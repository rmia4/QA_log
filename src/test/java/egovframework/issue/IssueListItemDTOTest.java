package egovframework.issue;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import egovframework.issue.dto.IssueListItemDTO;

public class IssueListItemDTOTest {

    @Test
    public void searchTextContainsTitleAndEntireIssueBody() {
        IssueListItemDTO issue = new IssueListItemDTO();
        issue.setTitle("로그인 오류");
        issue.setLocation("로그인 화면");
        issue.setLocationUrl("/login");
        issue.setDescription("버튼이 반응하지 않음");
        issue.setStepsToReproduce("아이디 입력 후 클릭");
        issue.setExpectedResult("메인 화면 이동");
        issue.setActualResult("현재 화면 유지");
        issue.setTestVersion("1.2.0");
        issue.setTestEnvironment("Chrome");
        issue.setSuggestedFix("이벤트 연결 확인");

        String searchText = issue.getSearchText();

        assertTrue(searchText.contains("로그인 오류"));
        assertTrue(searchText.contains("로그인 화면"));
        assertTrue(searchText.contains("/login"));
        assertTrue(searchText.contains("버튼이 반응하지 않음"));
        assertTrue(searchText.contains("아이디 입력 후 클릭"));
        assertTrue(searchText.contains("메인 화면 이동"));
        assertTrue(searchText.contains("현재 화면 유지"));
        assertTrue(searchText.contains("1.2.0"));
        assertTrue(searchText.contains("Chrome"));
        assertTrue(searchText.contains("이벤트 연결 확인"));
    }
}
