package mg.sw09.asig.service;

import java.util.List;

import mg.sw09.asig.entity.MemberDto;
import mg.sw09.asig.entity.PointDto;

public interface PointService {

    // 총 포인트 조회
    int getTotalPoint(String memId);

    // 회원 이름 조회
    String getMemberName(String memId);

    // 필터 + 페이징 포인트 내역 조회
    FilterResult getFilteredPoints(String memId, String filter, int currentPage);

    // 포인트 전환 (차감)
    ConversionResult convertPoints(String memId, int conversionPoint);

    // 출석체크
    CheckinResult checkIn(String memId);

    // ===== 결과 DTO들 =====

    class FilterResult {
        public final int total;
        public final int startPage;
        public final int endPage;
        public final int currentPage;
        public final int size;
        public final boolean hasPrevious;
        public final boolean hasNext;
        public final List<PointDto> pointList;
        public final String filter;

        public FilterResult(int total, int startPage, int endPage, int currentPage,
                            int size, boolean hasPrevious, boolean hasNext,
                            List<PointDto> pointList, String filter) {
            this.total = total;
            this.startPage = startPage;
            this.endPage = endPage;
            this.currentPage = currentPage;
            this.size = size;
            this.hasPrevious = hasPrevious;
            this.hasNext = hasNext;
            this.pointList = pointList;
            this.filter = filter;
        }
    }

    class ConversionResult {
        public final boolean success;
        public final int totalPoint;
        public final int remainingPoint;
        public final String errorMessage;

        public ConversionResult(boolean success, int totalPoint, int remainingPoint, String errorMessage) {
            this.success = success;
            this.totalPoint = totalPoint;
            this.remainingPoint = remainingPoint;
            this.errorMessage = errorMessage;
        }

        public static ConversionResult error(String msg) {
            return new ConversionResult(false, 0, 0, msg);
        }
    }

    class CheckinResult {
        public final MemberDto member;
        public final boolean alreadyCheckedIn;
        public final int addedPoint;

        public CheckinResult(MemberDto member, boolean alreadyCheckedIn, int addedPoint) {
            this.member = member;
            this.alreadyCheckedIn = alreadyCheckedIn;
            this.addedPoint = addedPoint;
        }
    }
}