package egovframework.common;

/** 로그인 후 여러 도메인이 함께 사용하는 세션 설정. */
public final class SessionKeys {

    public static final String LOGIN_USER_ID = "LOGIN_USER_ID";
    public static final int LOGIN_SESSION_SECONDS = 30 * 24 * 60 * 60;

    private SessionKeys() {
    }
}
