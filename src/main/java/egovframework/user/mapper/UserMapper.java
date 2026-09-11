package egovframework.user.mapper;

/**
 * TODO: 가입/로그인 구현 시 selectByLoginId, insertUser 등 추가하고 users.xml에 쿼리 작성.
 */
public interface UserMapper {

    /** 오류 도메인(담당자 변경 이력 표시 등)에서 이름만 필요할 때 쓰는 최소 조회. 존재하지 않는 id면 null. */
    String selectDisplayName(Long id);
}
