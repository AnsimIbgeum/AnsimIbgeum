package mg.sw09.asig.service;

import java.util.List;
import java.util.Map;

import mg.sw09.asig.entity.PopularWordDto;

public interface SearchWordService {

    /**
     * 로그인 여부/나이대에 따라 인기 검색어 리스트 조회
     *  - memId == null → 전체 인기 검색어
     *  - memId != null → 나이대별 인기 검색어
     */
    List<PopularWordDto> getPopularWordList(String memId);

    /**
     * 검색어를 검색하고, 로그인한 경우에는 popular_word 테이블에 기록까지 하는 기능
     */
    SearchResult searchAndRecord(String pwWord, String pageNo, String memId);

    /**
     * 단순히 검색 API만 호출 (페이지 이동 시 등)
     */
    SearchResult searchOnly(String pwWord, String pageNo);


    // ===== 결과 DTO =====
    class SearchResult {
        public final List<Map<String, Object>> resultList;
        public final String word;
        public final String totalCount;
        public final String currentPage;

        public SearchResult(List<Map<String, Object>> resultList,
                            String word,
                            String totalCount,
                            String currentPage) {
            this.resultList = resultList;
            this.word = word;
            this.totalCount = totalCount;
            this.currentPage = currentPage;
        }
    }
}