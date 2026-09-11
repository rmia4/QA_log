package egovframework.user.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import egovframework.user.vo.UserVO;
public interface UserMapper {

    UserVO selectByLoginId(@Param("loginId") String loginId);

    /** 오류 도메인(담당자 변경 이력 표시 등)에서 이름만 필요할 때 쓰는 최소 조회. 존재하지 않는 id면 null. */
    String selectDisplayName(Long id);

    /** 담당자 선택 드롭다운 구성용 - id/displayName만 채워진다(나머지 필드는 null). */
    List<UserVO> selectAllForOptions();
}
