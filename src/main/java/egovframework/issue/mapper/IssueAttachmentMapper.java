package egovframework.issue.mapper;

import java.util.List;

import egovframework.issue.vo.IssueAttachmentVO;

public interface IssueAttachmentMapper {

    void insertAttachment(IssueAttachmentVO attachment);

    /** 오류 본문에 딸린 첨부만(comment_id IS NULL) - 댓글 전용 첨부는 selectCommentAttachments 참고. */
    List<IssueAttachmentVO> selectAttachments(Long issueId);

    /** 이슈 전체 댓글의 첨부를 한 번에 조회한다(댓글별 그룹핑은 호출 측에서 처리). */
    List<IssueAttachmentVO> selectCommentAttachments(Long issueId);

    /** 첨부 다운로드/미리보기 엔드포인트에서 storage_key·mime_type 조회용. */
    IssueAttachmentVO selectAttachmentById(Long id);

    void deleteAttachment(Long id);
}
