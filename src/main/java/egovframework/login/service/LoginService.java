package egovframework.login.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import egovframework.user.mapper.UserMapper;
import egovframework.user.vo.UserVO;

@Service
public class LoginService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserVO authenticate(String loginId, String password) {
        String normalizedLoginId = loginId == null ? "" : loginId.trim();
        if (normalizedLoginId.isEmpty() || password == null || password.isEmpty()) {
            return null;
        }

        UserVO user = userMapper.selectByLoginId(normalizedLoginId);
        if (user == null || user.getPasswordHash() == null) {
            return null;
        }

        try {
            return passwordEncoder.matches(password, user.getPasswordHash()) ? user : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
