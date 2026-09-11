package egovframework.user.mapper;

import org.apache.ibatis.annotations.Param;

import egovframework.user.vo.UserVO;

public interface UserMapper {

    UserVO selectByLoginId(@Param("loginId") String loginId);
}
