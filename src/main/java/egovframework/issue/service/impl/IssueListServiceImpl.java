package egovframework.issue.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import egovframework.issue.dto.IssueListItemDTO;
import egovframework.issue.mapper.IssueMapper;
import egovframework.issue.service.IssueListService;

@Service
public class IssueListServiceImpl implements IssueListService {

    @Autowired
    private IssueMapper issueMapper;

    @Override
    public List<IssueListItemDTO> getIssueList(Long projectId, boolean closed) {
        return issueMapper.selectIssueList(projectId, closed);
    }
}
