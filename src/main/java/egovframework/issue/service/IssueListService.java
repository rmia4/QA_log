package egovframework.issue.service;

import java.util.List;

import egovframework.issue.dto.IssueListItemDTO;

public interface IssueListService {

    List<IssueListItemDTO> getIssueList(Long projectId, boolean closed, String sort, String direction);
}
