package egovframework.signup.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.team.mapper.TeamSettingsMapper;
import egovframework.user.mapper.UserMapper;
import egovframework.user.vo.UserVO;

@Service
public class SignupService {

    private final UserMapper userMapper;
    private final TeamSettingsMapper teamSettingsMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SignupService(UserMapper userMapper, TeamSettingsMapper teamSettingsMapper) {
        this.userMapper = userMapper;
        this.teamSettingsMapper = teamSettingsMapper;
    }

    @Transactional
    public SignupResult register(String inviteCode, String loginId, String password, String displayName) {
        String normalizedLoginId = trim(loginId);
        String normalizedDisplayName = trim(displayName);

        if (trim(inviteCode).isEmpty() || normalizedLoginId.isEmpty()
                || password == null || password.isEmpty() || normalizedDisplayName.isEmpty()) {
            return SignupResult.error("모든 항목을 입력해 주세요.");
        }
        if (normalizedLoginId.length() > 50) {
            return SignupResult.error("아이디는 50자 이하여야 합니다.");
        }
        if (normalizedDisplayName.length() > 50) {
            return SignupResult.error("화면 표시 이름은 50자 이하여야 합니다.");
        }
        if (!matchesInviteCode(inviteCode)) {
            return SignupResult.error("초대코드가 올바르지 않습니다.");
        }
        if (userMapper.selectByLoginId(normalizedLoginId) != null) {
            return SignupResult.error("이미 사용 중인 아이디입니다.");
        }

        UserVO user = new UserVO();
        user.setLoginId(normalizedLoginId);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setDisplayName(normalizedDisplayName);
        try {
            userMapper.insertUser(user);
        } catch (DataIntegrityViolationException ex) {
            return SignupResult.error("이미 사용 중인 아이디입니다.");
        }
        return SignupResult.success(user);
    }

    private boolean matchesInviteCode(String inviteCode) {
        String inviteCodeHash = teamSettingsMapper.selectInviteCodeHash();
        if (inviteCodeHash == null || inviteCodeHash.isEmpty()) {
            return false;
        }
        try {
            return passwordEncoder.matches(inviteCode, inviteCodeHash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public static final class SignupResult {
        private final UserVO user;
        private final String errorMessage;

        private SignupResult(UserVO user, String errorMessage) {
            this.user = user;
            this.errorMessage = errorMessage;
        }

        public static SignupResult success(UserVO user) {
            return new SignupResult(user, null);
        }

        public static SignupResult error(String errorMessage) {
            return new SignupResult(null, errorMessage);
        }

        public boolean isSuccess() {
            return user != null;
        }

        public UserVO getUser() {
            return user;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
