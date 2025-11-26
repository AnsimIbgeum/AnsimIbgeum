package mg.sw09.asig.controller;


import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import mg.sw09.asig.entity.BoardDto;
import mg.sw09.asig.entity.CommentDto;
import mg.sw09.asig.service.BoardService;
import mg.sw09.asig.service.BoardService.BoardDetailResult;
import mg.sw09.asig.service.BoardService.BoardListResult;
import mg.sw09.asig.service.BoardService.BoardSearchResult;
import mg.sw09.asig.service.BoardService.SuggestionResult;

@Controller
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 게시판 리스트
    @GetMapping("jgig/board_list")
    public String board_list_withPaging(@RequestParam(value = "pageNum", required = false) Integer pageNum,
                                        Model model) {

        BoardListResult result = boardService.getBoardList(pageNum);

        model.addAttribute("total", result.total);
        model.addAttribute("board_list", result.list);
        model.addAttribute("criteria", result.criteria);

        return "board/list";
    }

    // 게시글 보기
    @GetMapping("jgig/board_detail")
    public String board_detail(HttpSession session, @RequestParam("no") int no, Model model) {

        String mem_id = (String) session.getAttribute("mem_id");
        model.addAttribute("mem_id", mem_id);

        BoardDetailResult result = boardService.getBoardDetail(no);

        model.addAttribute("cmt_total", result.commentTotal);
        model.addAttribute("dto", result.board);
        model.addAttribute("cmt_list", result.commentList);

        return "board/detail";
    }

    // 게시글 작성
    @PostMapping("jgig/board_insert_action")
    public String board_insert_action(HttpSession session, BoardDto dto, Model model) {

        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        if (loggedIn == null || !loggedIn) {
            return "redirect:/jgig/login";
        }

        boardService.insertBoard(dto);
        model.addAttribute("msg", "게시물 추가 성공");
        return "board/insert_ok";
    }

    @GetMapping("jgig/board_insert_form")
    public String board_insert_form(HttpSession session) {

        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        if (loggedIn == null || !loggedIn) {
            return "redirect:/jgig/login";
        }
        return "board/insert_form";
    }

    // 게시글 수정
    @PostMapping("jgig/board_update_action")
    public String board_update_action(BoardDto dto, Model model) {

        boardService.updateBoard(dto);
        model.addAttribute("msg", "게시물 수정 성공");
        return "board/insert_ok";
    }

    @GetMapping("jgig/board_update_form")
    public String board_update_form(@RequestParam("no") int no, Model model) {

        // 수정 폼은 단순히 게시글만 필요
        BoardDetailResult result = boardService.getBoardDetail(no);
        model.addAttribute("dto", result.board);
        return "board/update_form";
    }

    // 게시글 삭제
    @PostMapping("jgig/board_delete_action")
    public String board_delete_action(BoardDto dto, Model model) {

        boardService.deleteBoard(dto);
        model.addAttribute("msg", "게시물 삭제 성공");
        return "board/insert_ok";
    }

    @GetMapping("jgig/board_delete_form")
    public String board_delete_form(@RequestParam("no") int no, Model model) {

        // 기존: commentMapper.deleteAll(no) + findByNo(no)
        // → 서비스에서 처리
        BoardDto dto = boardService.getBoardForDeleteForm(no);
        model.addAttribute("dto", dto);
        return "board/delete_form";
    }

    // 게시글 검색
    @GetMapping("jgig/board_search_action")
    public String board_search_list_withPaging(@RequestParam(value = "searchTag", required = false) String searchTag,
                                               @RequestParam(value = "keyword", required = false) String keyword,
                                               @RequestParam(value = "pageNum", required = false) Integer pageNum,
                                               Model model) {

        BoardSearchResult result = boardService.searchBoard(searchTag, keyword, pageNum);

        model.addAttribute("total", result.total);
        model.addAttribute("search_list", result.list);
        model.addAttribute("criteria", result.criteria);

        return "board/search_list";
    }

    // 댓글 작성
    @PostMapping("jgig/comment_insert_action")
    public String comment_insert_action(HttpSession session,
                                        @RequestParam(value = "brd_id", required = false) Integer brd_id,
                                        CommentDto dto,
                                        Model model) {

        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        if (loggedIn == null || !loggedIn) {
            return "redirect:/jgig/login";
        }

        boardService.insertComment(dto);
        model.addAttribute("brd_id", brd_id);
        model.addAttribute("msg", "댓글 작성 성공");
        return "board/comment_ok";
    }

    @GetMapping("jgig/comment_insert_form")
    public String comment_insert_form(HttpSession session, @RequestParam("no") int no, Model model) {

        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        if (loggedIn == null || !loggedIn) {
            return "redirect:/jgig/login";
        }

        String mem_id = (String) session.getAttribute("mem_id");
        model.addAttribute("mem_id", mem_id);

        BoardDetailResult result = boardService.getBoardDetail(no);
        model.addAttribute("dto", result.board);
        return "board/comment_insert_form";
    }

    // 댓글 수정
    @PostMapping("jgig/comment_update_action")
    public String comment_update_action(@RequestParam(value = "brd_id", required = false) Integer brd_id,
                                        CommentDto dto,
                                        Model model) {

        boardService.updateComment(dto);
        model.addAttribute("brd_id", brd_id);
        model.addAttribute("msg", "댓글 수정 성공");
        return "board/comment_ok";
    }

    @GetMapping("jgig/comment_update_form")
    public String comment_update_form(@RequestParam("cmt_no") int cmt_no, Model model) {

        CommentDto dto = boardService.getComment(cmt_no);
        model.addAttribute("dto", dto);
        return "board/comment_update_form";
    }

    // 댓글 삭제
    @PostMapping("jgig/comment_delete_action")
    public String board_comment_action(@RequestParam(value = "brd_id", required = false) Integer brd_id,
                                       CommentDto dto,
                                       Model model) {

        boardService.deleteComment(dto);
        model.addAttribute("brd_id", brd_id);
        model.addAttribute("msg", "댓글 삭제 성공");
        return "board/comment_ok";
    }

    @GetMapping("jgig/comment_delete_form")
    public String board_comment_form(@RequestParam("cmt_no") int cmt_no, Model model) {

        CommentDto dto = boardService.getComment(cmt_no);
        model.addAttribute("dto", dto);
        return "board/comment_delete_form";
    }

    // 내가 쓴 글
    @GetMapping("jgig/board_my_list_action")
    public String board_my_list_withPaging(HttpSession session,
                                           @RequestParam(value = "searchTag", required = false) String searchTag,
                                           @RequestParam(value = "keyword", required = false) String keyword,
                                           @RequestParam(value = "pageNum", required = false) Integer pageNum,
                                           Model model) {

        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        if (loggedIn == null || !loggedIn) {
            return "redirect:/jgig/login";
        }

        String mem_id = (String) session.getAttribute("mem_id");

        BoardSearchResult result = boardService.getMyBoardList(mem_id, pageNum);

        model.addAttribute("total", result.total);
        model.addAttribute("search_list", result.list);
        model.addAttribute("criteria", result.criteria);

        return "board/my_list";
    }

    // 게시글 추천
    @GetMapping("jgig/board_suggestion_action")
    public String board_suggestion_action(HttpSession session,
                                          @RequestParam(value = "brd_id", required = false) Integer brd_id,
                                          BoardDto dto,
                                          Model model) {

        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        if (loggedIn == null || !loggedIn) {
            return "redirect:/jgig/login";
        }

        String mem_id = (String) session.getAttribute("mem_id");

        SuggestionResult result = boardService.suggestBoard(brd_id, mem_id, dto);

        model.addAttribute("brd_id", brd_id);
        model.addAttribute("msg", result.message);

        return "board/comment_ok";
    }

    // 게시글 신고
    @GetMapping("jgig/board_declaration_action")
    public String board_declaration_action(HttpSession session,
                                           @RequestParam(value = "brd_id", required = false) Integer brd_id,
                                           BoardDto dto,
                                           Model model) {

        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        if (loggedIn == null || !loggedIn) {
            return "redirect:/jgig/login";
        }

        String mem_id = (String) session.getAttribute("mem_id");

        SuggestionResult result = boardService.declareBoard(brd_id, mem_id, dto);

        model.addAttribute("brd_id", brd_id);
        model.addAttribute("msg", result.message);

        return "board/comment_ok";
    }
}