package mg.sw09.asig.service;

import java.util.List;

import mg.sw09.asig.entity.Criteria;
import mg.sw09.asig.entity.NoticeDto;

public interface NoticeService {

    // 공지사항 리스트 (페이징)
    NoticeListResult getNoticeList(Integer pageNum);

    // 공지사항 상세
    NoticeDetailResult getNoticeDetail(int no);

    // 공지사항 검색 (제목/작성자)
    NoticeSearchResult searchNotice(String searchTag, String keyword, Integer pageNum);

    // ===== 결과 DTO들 =====

    class NoticeListResult {
        public final int total;
        public final List<NoticeDto> list;
        public final Criteria criteria;

        public NoticeListResult(int total, List<NoticeDto> list, Criteria criteria) {
            this.total = total;
            this.list = list;
            this.criteria = criteria;
        }
    }

    class NoticeDetailResult {
        public final NoticeDto notice;

        public NoticeDetailResult(NoticeDto notice) {
            this.notice = notice;
        }
    }

    class NoticeSearchResult {
        public final int total;
        public final List<NoticeDto> list;
        public final Criteria criteria;

        public NoticeSearchResult(int total, List<NoticeDto> list, Criteria criteria) {
            this.total = total;
            this.list = list;
            this.criteria = criteria;
        }
    }
}