package egovframework.common;

/**
 * 세션 속성 키 모음. 로그인 구현(동기 담당) 쪽에서 로그인 성공 시 이 키로 사용자 id(Long)를
 * 세션에 넣어주면, 그 외 모든 도메인(오류 등)이 같은 키로 "현재 로그인한 사용자"를 읽는다.
 * 아직 로그인 기능이 없어 실제로 세션에 값이 들어가지는 않는다 - 통합 시 동기와 이 키 이름을 맞출 것.
 */
public final class SessionKeys {

    public static final String LOGIN_USER_ID = "LOGIN_USER_ID";

    private SessionKeys() {
    }
}
