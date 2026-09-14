package egovframework.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DB에는 UTC로 저장하고(writing-block.md 공통 규칙), 화면에는 한국 시간(KST, UTC+9)으로 "yyyy-MM-dd HH:mm"만
 * 보여준다. 한국은 서머타임이 없어 항상 +9시간 고정이라 별도 타임존 변환 없이 더해도 정확하다.
 *
 * 주의: 낙관적 잠금에 쓰는 hidden expectedUpdatedAt 값은 이 포맷을 쓰면 안 된다 - 초 단위 이하가
 * 잘려서 DB의 실제 updated_at과 더 이상 정확히 일치하지 않게 되어 충돌 판정이 항상 실패하게 된다.
 * 그 값은 LocalDateTime.toString() 그대로(원본 정밀도 유지) 써야 한다.
 */
public final class KoreanDateTime {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private KoreanDateTime() {
    }

    public static String format(LocalDateTime utcValue) {
        if (utcValue == null) {
            return null;
        }
        return utcValue.plusHours(9).format(DISPLAY_FORMAT);
    }
}
