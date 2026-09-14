package egovframework.issue.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import egovframework.issue.dto.IssueCommentResponseDTO;

public interface IssueCommentService {

    /** 댓글마다 자신에게 딸린 첨부(attachments)를 채워서 반환한다. */
    List<IssueCommentResponseDTO> getComments(Long issueId);

    /**
     * 공백만 있는 내용은 저장하지 않는다(오류상세_기능명세서.md 4.5). files는 댓글에 딸린 스크린샷
     * 첨부(선택, 2026-09-14 추가) - 오류 본문 첨부와 달리 별도 이력을 남기지 않는다(댓글 자체가
     * 감사 기록만 남기고 화면 이력 탭에 노출 안 되는 것과 동일하게 취급).
     */
    Long addComment(Long issueId, String content, List<MultipartFile> files, Long actorId) throws IOException;
}
