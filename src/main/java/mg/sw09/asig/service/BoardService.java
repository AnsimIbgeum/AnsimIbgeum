package mg.sw09.asig.service;

import java.util.List;

import mg.sw09.asig.entity.BoardDto;
import mg.sw09.asig.entity.CommentDto;
import mg.sw09.asig.entity.Criteria;

public interface BoardService {

    // 게시글 리스트(페이징)
    BoardListResult getBoardList(Integer pageNum);

    // 게시글 상세 + 조회수 증가 + 댓글 목록/총 개수
    BoardDetailResult getBoardDetail(int no);

    // 게시글 작성/수정/삭제
    void insertBoard(BoardDto dto);
    void updateBoard(BoardDto dto);
    void deleteBoard(BoardDto dto);

    // 삭제 폼용: 댓글 삭제 후 게시글 조회
    BoardDto getBoardForDeleteForm(int no);

    // 게시글 검색(제목/작성자)
    BoardSearchResult searchBoard(String searchTag, String keyword, Integer pageNum);

    // 내가 쓴 글 목록
    BoardSearchResult getMyBoardList(String memId, Integer pageNum);

    // 댓글 작성/수정/삭제/조회
    void insertComment(CommentDto dto);
    void updateComment(CommentDto dto);
    void deleteComment(CommentDto dto);
    CommentDto getComment(int cmtNo);

    // 추천/신고
    SuggestionResult suggestBoard(Integer brdId, String memId, BoardDto dto);
    SuggestionResult declareBoard(Integer brdId, String memId, BoardDto dto);

    class BoardListResult {
        public final int total;
        public final List<BoardDto> list;
        public final Criteria criteria;

        public BoardListResult(int total, List<BoardDto> list, Criteria criteria) {
            this.total = total;
            this.list = list;
            this.criteria = criteria;
        }
    }

    class BoardDetailResult {
        public final BoardDto board;
        public final int commentTotal;
        public final List<CommentDto> commentList;

        public BoardDetailResult(BoardDto board, int commentTotal, List<CommentDto> commentList) {
            this.board = board;
            this.commentTotal = commentTotal;
            this.commentList = commentList;
        }
    }

    class BoardSearchResult {
        public final int total;
        public final List<BoardDto> list;
        public final Criteria criteria;

        public BoardSearchResult(int total, List<BoardDto> list, Criteria criteria) {
            this.total = total;
            this.list = list;
            this.criteria = criteria;
        }
    }

    class SuggestionResult {
        public final boolean success;
        public final String message;

        public SuggestionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}