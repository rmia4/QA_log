package egovframework.issue.mapper;

import java.util.List;

import egovframework.issue.vo.IssueAttachmentVO;

public interface IssueAttachmentMapper {

    void insertAttachment(IssueAttachmentVO attachment);

    List<IssueAttachmentVO> selectAttachments(Long issueId);

    /** 첨부 다운로드/미리보기 엔드포인트에서 storage_key·mime_type 조회용. */
    IssueAttachmentVO selectAttachmentById(Long id);

    void deleteAttachment(Long id);
}
