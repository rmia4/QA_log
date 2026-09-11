package egovframework.issue.dto.request;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/** 종료/재오픈처럼 값 하나 없이 상태만 바뀌는 요청의 낙관적 잠금 확인용 최소 바디. */
@Getter
@Setter
public class IssueConcurrencyRequestDTO {
    private LocalDateTime expectedUpdatedAt;
}
