package mg.sw09.asig.controller;

import mg.sw09.asig.service.MainService;
import mg.sw09.asig.service.MainService.AttendanceData;
import mg.sw09.asig.service.MainService.MainPageData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class MainController {

    private final MainService mainService;

    public MainController(MainService mainService) {
        this.mainService = mainService;
    }

    @GetMapping("/jgig/")
    public String mainpage(HttpSession session, Model model) {

        MainPageData data = mainService.getMainPageData();

        model.addAttribute("free_list", data.freeList);
        model.addAttribute("question_list", data.questionList);
        model.addAttribute("notice_list", data.noticeList);
        model.addAttribute("point_rank", data.pointRank);

        return "index";
    }

    @GetMapping("/jgig/attendance")
    public String attendance(HttpSession session, Model model) {

        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return returnVal;

        String mem_id = returnVal;

        AttendanceData att = mainService.getAttendanceData(mem_id);

        model.addAttribute("att_yes", att.attYes);
        model.addAttribute("dayOfMonth", att.lastDayOfMonth);

        return "point/attendance";
    }

    public String login_check(HttpSession session) { // 로그인 체크 함수
        // 세션에서 로그인 정보를 확인
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        // 로그인 여부를 확인하고, 로그인되지 않은 경우 로그인 페이지로 리다이렉트
        if (loggedIn == null || !loggedIn) {
            return "redirect:/jgig/login";
        }

        // 로그인된 경우, 세션에서 로그인 아이디 받아오기.
        String mem_id = (String) session.getAttribute("mem_id");
        return mem_id;
    }
}