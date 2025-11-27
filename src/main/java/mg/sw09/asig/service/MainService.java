package mg.sw09.asig.service;

import java.util.List;

import mg.sw09.asig.entity.BoardDto;
import mg.sw09.asig.entity.MemberDto;
import mg.sw09.asig.entity.NoticeDto;

public interface MainService {

    // 메인 페이지에 필요한 데이터
    MainPageData getMainPageData();

    // 출석 페이지 데이터 (특정 사용자 기준)
    AttendanceData getAttendanceData(String memId);

    // ===== 결과 DTO들 =====

    class MainPageData {
        public final List<BoardDto> freeList;
        public final List<BoardDto> questionList;
        public final List<NoticeDto> noticeList;
        public final List<MemberDto> pointRank;

        public MainPageData(List<BoardDto> freeList,
                            List<BoardDto> questionList,
                            List<NoticeDto> noticeList,
                            List<MemberDto> pointRank) {
            this.freeList = freeList;
            this.questionList = questionList;
            this.noticeList = noticeList;
            this.pointRank = pointRank;
        }
    }

    class AttendanceData {
        public final java.util.List<Integer> attYes; // 출석한 날짜 리스트 (인덱스 = 일자)
        public final int lastDayOfMonth;             // 이번 달 마지막 날(예: 30, 31 등)

        public AttendanceData(java.util.List<Integer> attYes, int lastDayOfMonth) {
            this.attYes = attYes;
            this.lastDayOfMonth = lastDayOfMonth;
        }
    }
}