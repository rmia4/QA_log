package egovframework.user.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * users 테이블 1:1 대응. writing-block.md 3절 참고.
 * 가입/로그인 화면(동기 담당) 구현 시 Mapper.xml(users.xml)과 함께 채워 넣을 것.
 */
@Getter
@Setter
public class UserVO {

    private Long id;
    private String loginId;
    private String passwordHash;
    private String displayName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
