package mg.sw09.asig.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import mg.sw09.asig.service.NoticeService;
import mg.sw09.asig.service.NoticeService.NoticeDetailResult;
import mg.sw09.asig.service.NoticeService.NoticeListResult;
import mg.sw09.asig.service.NoticeService.NoticeSearchResult;

@Controller
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    // 공지사항 리스트
    @GetMapping("jgig/notice_list")
    public String notice_list_withPaging(@RequestParam(value = "pageNum", required = false) Integer pageNum,
                                         Model model) {

        NoticeListResult result = noticeService.getNoticeList(pageNum);

        model.addAttribute("total", result.total);
        model.addAttribute("notice_list", result.list);
        model.addAttribute("criteria", result.criteria);

        return "notice/list";
    }

    // 공지사항 보기
    @GetMapping("jgig/notice_detail")
    public String notice_detail(@RequestParam("no") int no, Model model) {

        NoticeDetailResult result = noticeService.getNoticeDetail(no);
        model.addAttribute("dto", result.notice);

        return "notice/detail";
    }

    // 공지사항 검색
    @GetMapping("jgig/notice_search_action")
    public String notice_search_list_withPaging(
            @RequestParam(value = "searchTag", required = false) String searchTag,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            Model model) {

        NoticeSearchResult result = noticeService.searchNotice(searchTag, keyword, pageNum);

        model.addAttribute("total", result.total);
        model.addAttribute("search_list", result.list);
        model.addAttribute("criteria", result.criteria);

        return "notice/search_list";
    }
}