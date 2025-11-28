package mg.sw09.asig.util;

public class MaskingUtil {

    // 주민번호 마스킹 (990101-1******)
    public static String maskSSN(String ssn) {
        if (ssn == null || ssn.length() < 8) return ssn;
        return ssn.substring(0, 8) + "******";
    }

    // 카드번호 마스킹 (1234-****-****-5678)
    public static String maskCardNum(String cardNum) {
        if (cardNum == null || cardNum.length() < 16) return cardNum;

        String cleanNum = cardNum.replaceAll("-", "");

        if (cleanNum.length() == 16) {
            return cleanNum.substring(0, 4) + "-****-****-" + cleanNum.substring(12);
        }
        return cardNum;
    }
}