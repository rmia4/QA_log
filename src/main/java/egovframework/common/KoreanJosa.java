package egovframework.common;

/**
 * 한글 받침 유무에 따라 조사(을/를, 로/으로)를 고른다. 유니코드 완성형 한글(가~힣, U+AC00~U+D7A3)에서
 * (코드 - 0xAC00) % 28 == 0이면 받침이 없다(28 = 종성 슬롯 수, 0번째가 "받침 없음").
 */
public final class KoreanJosa {

    private static final int HANGUL_BASE = 0xAC00;
    private static final int HANGUL_LAST = 0xD7A3;
    private static final int JONGSUNG_COUNT = 28;

    private KoreanJosa() {
    }

    private static boolean hasBatchim(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        char last = word.charAt(word.length() - 1);
        if (last < HANGUL_BASE || last > HANGUL_LAST) {
            return false;
        }
        return (last - HANGUL_BASE) % JONGSUNG_COUNT != 0;
    }

    /** "을" 또는 "를" */
    public static String eulReul(String word) {
        return hasBatchim(word) ? "을" : "를";
    }

    /** "으로" 또는 "로" */
    public static String roEuro(String word) {
        return hasBatchim(word) ? "으로" : "로";
    }
}
