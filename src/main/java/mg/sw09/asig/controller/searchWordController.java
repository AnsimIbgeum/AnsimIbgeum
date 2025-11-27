package mg.sw09.asig.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import mg.sw09.asig.entity.PopularWordDto;
import mg.sw09.asig.service.SearchWordService;
import mg.sw09.asig.service.SearchWordService.SearchResult;

import java.util.List;

@Controller
public class searchWordController {

    private final SearchWordService searchWordService;

    public searchWordController(SearchWordService searchWordService) {
        this.searchWordService = searchWordService;
    }

    @GetMapping("/jgig/searchWord")
    public String loadSearchWord(Model model, HttpSession session) {
        String memId = (String) session.getAttribute("mem_id");

        // 로그인 유무에 따라 인기 검색어 조회
        List<PopularWordDto> list = searchWordService.getPopularWordList(memId);
        model.addAttribute("word_list", list);

        return "search_word/searchWord";
    }

    @PostMapping("/jgig/searchWordResult")
    public String getSearchWordHandler(@RequestParam("pw_word") String pw_word,
                                       @RequestParam("pageNo") String pageNo,
                                       Model model,
                                       HttpSession session) {

        String memId = (String) session.getAttribute("mem_id");

        // 검색 + (로그인 시) 기록
        SearchResult result = searchWordService.searchAndRecord(pw_word, pageNo, memId);

        model.addAttribute("result_list", result.resultList);
        model.addAttribute("word", result.word);
        model.addAttribute("totalCount", result.totalCount);
        model.addAttribute("currentPage", result.currentPage);

        return "search_word/searchWordResult";
    }

    @PostMapping("/jgig/searchWordResult/{pageNo}")
    public String postSearchWordHandler(@RequestParam("pw_word") String pw_word,
                                        @PathVariable("pageNo") String pageNo,
                                        Model model) {

        SearchResult result = searchWordService.searchOnly(pw_word, pageNo);

        model.addAttribute("result_list", result.resultList);
        model.addAttribute("word", result.word);
        model.addAttribute("totalCount", result.totalCount);

        return "search_word/resultList";
    }
}