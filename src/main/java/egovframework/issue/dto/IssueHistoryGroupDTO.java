package egovframework.issue.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 상세 화면 "변경 이력" 탭 전용 화면 표시 모델. 같은 change_group_id(한 번의 저장 요청)로 묶인
 * IssueHistoryResponseDTO 여러 건을 하나로 합친다 - 오류 등록/수정 시 필드 여러 개 + 첨부 추가/삭제가
 * 한꺼번에 이력에 남아 화면에 줄이 너무 많아지는 문제를 해결하기 위함(2026-09-14 팀 결정).
 * 항목이 1건이면 요약 없이 그 항목 원문 그대로 보여주고, 2건 이상이면 "변경 N건" 요약 + 펼치기로 표시한다.
 */
@Getter
@Setter
public class IssueHistoryGroupDTO {

    private String actorName;
    private String createdAtDisplay;
    private List<IssueHistoryResponseDTO> items;

    public int getCount() {
        return items.size();
    }

    public IssueHistoryResponseDTO getSingle() {
        return items.get(0);
    }
}
