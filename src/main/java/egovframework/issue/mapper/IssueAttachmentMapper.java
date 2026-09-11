package egovframework.issue.mapper;

import java.util.List;

import egovframework.issue.vo.IssueAttachmentVO;

public interface IssueAttachmentMapper {

    void insertAttachment(IssueAttachmentVO attachment);

    List<IssueAttachmentVO> selectAttachments(Long issueId);
}
