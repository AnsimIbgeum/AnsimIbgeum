package mg.sw09.asig.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mg.sw09.asig.entity.BoardDto;
import mg.sw09.asig.entity.CommentDto;
import mg.sw09.asig.entity.Criteria;
import mg.sw09.asig.entity.SuggestionDto;
import mg.sw09.asig.mapper.BoardMapper;
import mg.sw09.asig.mapper.CommentMapper;

@Service
public class BoardServiceImpl implements BoardService {

    private final BoardMapper boardMapper;
    private final CommentMapper commentMapper;

    @Autowired
    public BoardServiceImpl(BoardMapper boardMapper, CommentMapper commentMapper) {
        this.boardMapper = boardMapper;
        this.commentMapper = commentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public BoardListResult getBoardList(Integer pageNum) {
        int total = boardMapper.getTotal();

        if (pageNum == null) {
            pageNum = 1;
        }

        Criteria criteria = new Criteria(pageNum, total);
        List<BoardDto> list = boardMapper.listWithPaging(criteria);

        return new BoardListResult(total, list, criteria);
    }

    @Override
    @Transactional
    public BoardDetailResult getBoardDetail(int no) {
        int cmtTotal = commentMapper.getTotal(no);

        BoardDto dto = boardMapper.findByNo(no);
        boardMapper.updateView(dto);

        List<CommentDto> cmtList = commentMapper.list(no);

        return new BoardDetailResult(dto, cmtTotal, cmtList);
    }

    @Override
    @Transactional
    public void insertBoard(BoardDto dto) {
        boardMapper.insert(dto);
    }

    @Override
    @Transactional
    public void updateBoard(BoardDto dto) {
        boardMapper.update(dto);
    }

    @Override
    @Transactional
    public void deleteBoard(BoardDto dto) {
        boardMapper.delete(dto);
    }

    @Override
    @Transactional
    public BoardDto getBoardForDeleteForm(int no) {
        // 기존 코드: delete_form에서 댓글 전체 삭제 후 게시글 조회
        commentMapper.deleteAll(no);
        return boardMapper.findByNo(no);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardSearchResult searchBoard(String searchTag, String keyword, Integer pageNum) {

        if (pageNum == null) {
            pageNum = 1;
        }

        Criteria criteria;
        List<BoardDto> list;
        int total;

        if ("제목".equals(searchTag)) {
            total = boardMapper.getSearchTotalByTitle(keyword);
            criteria = new Criteria(pageNum, searchTag, keyword, total);
            list = boardMapper.searchListWithPagingByTitle(criteria);
        } else {
            total = boardMapper.getSearchTotalByMem(keyword);
            criteria = new Criteria(pageNum, searchTag, keyword, total);
            list = boardMapper.searchListWithPagingByMem(criteria);
        }

        return new BoardSearchResult(total, list, criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardSearchResult getMyBoardList(String memId, Integer pageNum) {

        if (pageNum == null) {
            pageNum = 1;
        }

        String searchTag = null; // 기존 Criteria 생성 방식 유지
        int total = boardMapper.getSearchTotalByMem(memId);
        Criteria criteria = new Criteria(pageNum, searchTag, memId, total);
        List<BoardDto> list = boardMapper.searchListWithPagingByMem(criteria);

        return new BoardSearchResult(total, list, criteria);
    }

    @Override
    @Transactional
    public void insertComment(CommentDto dto) {
        commentMapper.insert(dto);
    }

    @Override
    @Transactional
    public void updateComment(CommentDto dto) {
        commentMapper.update(dto);
    }

    @Override
    @Transactional
    public void deleteComment(CommentDto dto) {
        commentMapper.deleteOne(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getComment(int cmtNo) {
        return commentMapper.findByNo(cmtNo);
    }

    @Override
    @Transactional
    public SuggestionResult suggestBoard(Integer brdId, String memId, BoardDto dto) {

        if (boardMapper.isSuggestion(brdId, memId) == 0) {
            SuggestionDto sdto = new SuggestionDto(brdId, memId);
            boardMapper.suggestion(sdto);
            boardMapper.updateSuggestion(dto);
            return new SuggestionResult(true, "공감 완료");
        } else {
            return new SuggestionResult(false, "이미 공감한 글입니다.");
        }
    }

    @Override
    @Transactional
    public SuggestionResult declareBoard(Integer brdId, String memId, BoardDto dto) {

        if (boardMapper.isDeclaration(brdId, memId) == 0) {
            SuggestionDto sdto = new SuggestionDto(brdId, memId);
            boardMapper.declaration(sdto);
            boardMapper.updatesDeclaration(dto);
            return new SuggestionResult(true, "반대 완료");
        } else {
            return new SuggestionResult(false, "이미 반대한 글입니다.");
        }
    }
}