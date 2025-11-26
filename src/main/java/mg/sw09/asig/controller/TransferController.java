package mg.sw09.asig.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import mg.sw09.asig.entity.AccountDto;
import mg.sw09.asig.entity.TransferDto;
import mg.sw09.asig.service.TransferService;
import mg.sw09.asig.service.TransferService.HistoryResult;
import mg.sw09.asig.service.TransferService.TransferResult;

@Controller
public class TransferController {

    private final TransferService transferService;
    public final int point = 5; // (지금은 Service에서 쓰므로 사실 여기선 안 써도 됨)

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    // 계좌이체 입력(입금은행, 입금계좌번호, 이체금액, 계좌번호)
    @GetMapping("jgig/transfer_form")
    public String transfer_form(@RequestParam("account") long account, Model model) {
        AccountDto accountDto = transferService.getAccountForTransfer(account);
        model.addAttribute("dto", accountDto);
        return "transfer/transfer_form";
    }

    // 계좌이체 입력(입력내용확인)
    @PostMapping("jgig/transfer_form2")
    public String transfer_form_action(TransferDto transferDto,
                                       Model model,
                                       @RequestParam("act_password") int act_password,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {

        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";

        boolean correct = transferService.checkAccountPassword(transferDto.getAccount(), act_password);
        if (!correct) {
            redirectAttributes.addFlashAttribute("transDto", transferDto);
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호가 맞지 않습니다.");
            return "redirect:/jgig/transfer_form?account=" + transferDto.getAccount();
        }

        model.addAttribute("dto", transferDto);
        return "transfer/transfer_form2";
    }

    // 계좌이체 액션
    @PostMapping("jgig/transfer_action")
    public String transfer_action(TransferDto transferDto, HttpSession session, Model model) {

        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";

        String mem_id = returnVal;

        TransferResult result = transferService.executeTransfer(transferDto, mem_id);

        if (result.success) {
            model.addAttribute("dto", result.dto);
            model.addAttribute("msg", result.msg);
            model.addAttribute("point", result.pointMessage);
            return "transfer/transfer_ok";
        } else {
            model.addAttribute("msg", result.msg);
            return "transfer/transfer_fail";
        }
    }

    // 거래내역조회 폼
    @GetMapping("jgig/trans_history")
    public String trans_history(Model model, HttpSession session) {
        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";

        String mem_id = returnVal;
        List<TransferDto> accountList = transferService.getAccountList(mem_id);
        model.addAttribute("accountList", accountList);
        return "transfer/trans_history";
    }

    // 거래내역조회 액션1(월별조회)
    @PostMapping("jgig/trans_history_action")
    public String trans_history_action(@RequestParam("selectedAccount") long selectedAccount,
                                       @RequestParam(name = "currentPage", defaultValue = "1") int currentPage,
                                       @RequestParam("year") int year,
                                       @RequestParam("month") int month,
                                       HttpSession session,
                                       Model model) {

        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";
        String mem_id = returnVal;

        HistoryResult hr = transferService.getMonthlyHistory(selectedAccount, currentPage, year, month, mem_id, "월별조회");

        model.addAttribute("total", hr.total);
        model.addAttribute("startPage", hr.startPage);
        model.addAttribute("endPage", hr.endPage);
        model.addAttribute("currentPage", hr.currentPage);
        model.addAttribute("size", hr.size);
        model.addAttribute("hasPrevious", hr.hasPrevious);
        model.addAttribute("hasNext", hr.hasNext);
        model.addAttribute("transferList", hr.transferList);

        return "transfer/transfer_history_table";
    }

    // 거래내역조회 액션2(달력조회)
    @PostMapping("jgig/trans_history_action2")
    public String trans_history_action2(@RequestParam("selectedAccount") long selectedAccount,
                                        @RequestParam(name = "currentPage", defaultValue = "1") int currentPage,
                                        @RequestParam("startDate") String startDate,
                                        @RequestParam("endDate") String endDate,
                                        HttpSession session,
                                        Model model) {

        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";
        String mem_id = returnVal;

        HistoryResult hr = transferService.getCalendarHistory(selectedAccount, currentPage, startDate, endDate, mem_id, "달력조회");

        model.addAttribute("total", hr.total);
        model.addAttribute("startPage", hr.startPage);
        model.addAttribute("endPage", hr.endPage);
        model.addAttribute("currentPage", hr.currentPage);
        model.addAttribute("size", hr.size);
        model.addAttribute("hasPrevious", hr.hasPrevious);
        model.addAttribute("hasNext", hr.hasNext);
        model.addAttribute("transferList", hr.transferList);

        return "transfer/transfer_history_table2";
    }

    // 거래내역조회 폼(계좌관리에서 해당 계좌로 들어온 경우)
    @GetMapping("jgig/trans_history_selected")
    public String trans_history_selected(@RequestParam("account") long account,
                                         HttpSession session,
                                         Model model) {

        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";

        model.addAttribute("account", account);
        return "transfer/trans_history_selected";
    }

    // 거래내역조회 액션1(계좌관리에서 해당 계좌로 들어온 경우)(월별조회)
    @PostMapping("jgig/trans_history_selected_action")
    public String trans_history_selected_action(@RequestParam("selectedAccount") long selectedAccount,
                                                @RequestParam(name = "currentPage", defaultValue = "1") int currentPage,
                                                @RequestParam("year") int year,
                                                @RequestParam("month") int month,
                                                HttpSession session,
                                                Model model) {

        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";
        String mem_id = returnVal;

        HistoryResult hr = transferService.getMonthlyHistory(selectedAccount, currentPage, year, month, mem_id, "월별조회");

        model.addAttribute("total", hr.total);
        model.addAttribute("startPage", hr.startPage);
        model.addAttribute("endPage", hr.endPage);
        model.addAttribute("currentPage", hr.currentPage);
        model.addAttribute("size", hr.size);
        model.addAttribute("hasPrevious", hr.hasPrevious);
        model.addAttribute("hasNext", hr.hasNext);
        model.addAttribute("transferList", hr.transferList);

        return "transfer/transfer_history_table";
    }

    // 거래내역조회 액션2(계좌관리에서 해당 계좌로 들어온 경우)(달력조회)
    @PostMapping("jgig/trans_history_selected_action2")
    public String trans_history_selected_action2(@RequestParam("selectedAccount") long selectedAccount,
                                                 @RequestParam(name = "currentPage", defaultValue = "1") int currentPage,
                                                 @RequestParam("startDate") String startDate,
                                                 @RequestParam("endDate") String endDate,
                                                 HttpSession session,
                                                 Model model) {

        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";
        String mem_id = returnVal;

        HistoryResult hr = transferService.getCalendarHistory(selectedAccount, currentPage, startDate, endDate, mem_id, "달력조회");

        model.addAttribute("total", hr.total);
        model.addAttribute("startPage", hr.startPage);
        model.addAttribute("endPage", hr.endPage);
        model.addAttribute("currentPage", hr.currentPage);
        model.addAttribute("size", hr.size);
        model.addAttribute("hasPrevious", hr.hasPrevious);
        model.addAttribute("hasNext", hr.hasNext);
        model.addAttribute("transferList", hr.transferList);

        return "transfer/transfer_history_table2";
    }

    // 로그인 체크 (Controller 책임으로 유지)
    private String login_check(HttpSession session) {
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        if (loggedIn == null || !loggedIn) {
            return "redirect:/jgig/login";
        }
        String mem_id = (String) session.getAttribute("mem_id");
        return mem_id;
    }
}