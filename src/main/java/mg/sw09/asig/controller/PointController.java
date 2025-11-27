package mg.sw09.asig.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import mg.sw09.asig.service.PointService;
import mg.sw09.asig.service.PointService.CheckinResult;
import mg.sw09.asig.service.PointService.ConversionResult;
import mg.sw09.asig.service.PointService.FilterResult;

@Controller
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    // 초기 총 포인트 계산 및 모델에 추가
    @ModelAttribute("totalPoint")
    public int totalPoint(HttpSession session) {
        String memId = (String) session.getAttribute("mem_id");
        if (memId != null) {
            return pointService.getTotalPoint(memId);
        }
        return 0;
    }

    // 회원 이름 가져오기
    @ModelAttribute("memberName")
    public String memberName(HttpSession session) {
        String memId = (String) session.getAttribute("mem_id");
        if (memId != null) {
            return pointService.getMemberName(memId);
        }
        return "";
    }

    @GetMapping("/jgig/point_list") // 포인트 내역 조회 페이지
    public String pointListPage(HttpSession session) {
        String memId = (String) session.getAttribute("mem_id");
        if (memId == null) {
            return "redirect:/jgig/login";
        }
        return "point/list";
    }

    @PostMapping("/jgig/point_list") // 필터링된 포인트 내역 조회
    public String filterPointList(@RequestParam("filter") String filter,
                                  HttpSession session,
                                  @RequestParam(name = "currentPage", defaultValue = "1") int currentPage,
                                  Model model) {
        String memId = (String) session.getAttribute("mem_id");
        if (memId == null) {
            return "redirect:/jgig/login";
        }

        FilterResult fr = pointService.getFilteredPoints(memId, filter, currentPage);

        model.addAttribute("total", fr.total);
        model.addAttribute("startPage", fr.startPage);
        model.addAttribute("endPage", fr.endPage);
        model.addAttribute("currentPage", fr.currentPage);
        model.addAttribute("size", fr.size);
        model.addAttribute("hasPrevious", fr.hasPrevious);
        model.addAttribute("hasNext", fr.hasNext);
        model.addAttribute("pointList", fr.pointList);
        model.addAttribute("filter", fr.filter);

        return "point/list_filter";
    }

    @GetMapping("/jgig/point_conversion") // 포인트 전환 페이지
    public String pointConversionPage(Model model, HttpSession session) {
        String memId = (String) session.getAttribute("mem_id");
        if (memId != null) {
            int totalPoint = pointService.getTotalPoint(memId);
            model.addAttribute("totalPoint", totalPoint);
            return "point/transform";
        }
        return "redirect:/jgig/login";
    }

    @PostMapping("/jgig/point_conversion") // 포인트 전환 처리
    public String convertPoints(@RequestParam("conversionPoint") int conversionPoint,
                                HttpSession session,
                                Model model) {
        String memId = (String) session.getAttribute("mem_id");
        if (memId == null) {
            return "redirect:/jgig/login";
        }

        ConversionResult cr = pointService.convertPoints(memId, conversionPoint);

        if (!cr.success) {
            // 남은 포인트 음수 등 에러 케이스
            return "point/conversion_error";
        }

        model.addAttribute("totalPoint", cr.totalPoint);
        model.addAttribute("remainingPoint", cr.remainingPoint);
        model.addAttribute("conversionSuccess", true);

        return "point/transform";
    }

    @PostMapping("jgig/checkin")
    public String checkIn(HttpSession session, Model model) {
        String memId = (String) session.getAttribute("mem_id");
        if (memId == null) {
            return "redirect:/jgig/login";
        }

        CheckinResult result = pointService.checkIn(memId);

        if (result.member != null) {
            model.addAttribute("memberDto", result.member);
        }

        // 출석체크 여부를 세션에 저장 (기존 로직 유지)
        if (result.alreadyCheckedIn) {
            session.setAttribute("checkinSuccess", false);
        } else {
            session.setAttribute("checkinSuccess", true);
        }
        return "redirect:/jgig/attendance";
    }
}