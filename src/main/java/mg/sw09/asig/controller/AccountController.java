package mg.sw09.asig.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import mg.sw09.asig.entity.AccountDto;
import mg.sw09.asig.service.AccountService;
import mg.sw09.asig.service.AccountService.AccountListResult;
import mg.sw09.asig.service.AccountService.OpenAccountResult;
import mg.sw09.asig.service.AccountService.TerminationResult;
import mg.sw09.asig.service.AccountService.UpdatePasswordResult;


@Controller
public class AccountController {

    @Autowired
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    String ssn, phone_num, actName, memName, mem_id;
    public final int point = 5;

    public String login_check(HttpSession session) {
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        if (loggedIn == null || !loggedIn) {
            return "redirect:/jgig/login";
        }
        String mem_id = (String) session.getAttribute("mem_id");
        return mem_id;
    }

    @GetMapping("/jgig/open_account1")
    public String open_form1(HttpSession session) {
        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";

        return "account/open_account1";
    }

    @PostMapping("/jgig/open_account2")
    public String open_form2(HttpSession session, Model model) {

        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";

        model.addAttribute("act_name", actName);
        model.addAttribute("mem_nm", memName);
        model.addAttribute("ssn", ssn);
        model.addAttribute("phone_num", phone_num);
        model.addAttribute("mem_id", mem_id);

        return "account/open_account2";
    }

    @PostMapping("/jgig/member_check")
    @ResponseBody
    public Map<String, Object> member_check(@RequestParam("ssn1") String ssn1, @RequestParam("ssn2") String ssn2,
                                            @RequestParam("act_name") String act_name, @RequestParam("mem_nm") String mem_nm,
                                            @RequestParam("phone_num1") String phone_num1, @RequestParam("phone_num2") String phone_num2,
                                            @RequestParam("phone_num3") String phone_num3, HttpSession session, Model model) {

        Map<String, Object> response = new HashMap<>();

        actName = act_name;
        ssn = ssn1 + ssn2;
        phone_num = phone_num1 + phone_num2 + phone_num3;
        memName = mem_nm;

        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login")) {
            response.put("success", false);
            return response;
        }
        mem_id = returnVal;

        String memIdFromSession = (String) session.getAttribute("mem_id");
        String memPhoneFromSession = (String) session.getAttribute("phone_num");
        String memSsnFromSession = (String) session.getAttribute("ssn");

        boolean success = accountService.checkMember(
                memIdFromSession,
                memPhoneFromSession,
                memSsnFromSession,
                mem_nm,
                ssn,
                phone_num
        );

        response.put("success", success);
        return response;
    }

    @PostMapping("/jgig/open_action")
    public String open_action(AccountDto dto, HttpSession session, Model model) {
        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";

        String memId = (String) session.getAttribute("mem_id");

        OpenAccountResult result = accountService.openAccount(dto, memId);

        model.addAttribute("dto", dto);
        model.addAttribute("account_num", result.accountNum);
        model.addAttribute("msg", "해당 계좌의 개설이 완료되었습니다.");

        if (result.alreadyPracticed) {
            model.addAttribute("point", "이미 계좌개설 연습을 하였습니다.");
        } else {
            model.addAttribute("point", "포인트가 " + result.point + " 적립되었습니다.");
        }

        return "account/open_ok";
    }

    @GetMapping("/jgig/account_list")
    public String account_list(Model model, HttpSession session,
                               @RequestParam(name = "currentPage", defaultValue = "1") int currentPage) {

        String returnVal = login_check(session);

        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";

        String mem_id = returnVal;

        AccountListResult result = accountService.getAccountList(mem_id, currentPage);

        if (result.list.isEmpty()) {
            return "account/no_account";
        }

        model.addAttribute("total", result.total);
        model.addAttribute("startPage", result.startPage);
        model.addAttribute("endPage", result.endPage);
        model.addAttribute("currentPage", result.currentPage);
        model.addAttribute("totalBalance", result.totalBalance);
        model.addAttribute("size", result.size);
        model.addAttribute("account_list", result.list);
        model.addAttribute("hasPrevious", result.hasPrevious);
        model.addAttribute("hasNext", result.hasNext);

        return "account/list";
    }

    @GetMapping("/jgig/account_management")
    public String account_management(Model model, HttpSession session, @RequestParam("account") long account) {
        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";

        AccountDto dto = accountService.getAccount(account);
        Date regdate = dto.getRegdate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String formattedRegdate = dateFormat.format(regdate);

        model.addAttribute("formattedRegdate", formattedRegdate);
        model.addAttribute("dto", dto);
        return "account/management";
    }

    @GetMapping("/jgig/update_password")
    public String board_update_form(Model model, HttpSession session, @RequestParam("account") long account) {
        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";

        AccountDto dto = accountService.getAccount(account);
        model.addAttribute("dto", dto);
        return "account/update_password";
    }

    @PostMapping("/jgig/update_password_action")
    public String update_password_action(AccountDto dto, HttpSession session, Model model) {
        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";

        String memId = (String) session.getAttribute("mem_id");

        UpdatePasswordResult result = accountService.updatePassword(dto, memId);

        model.addAttribute("dto", dto);
        model.addAttribute("msg", "비밀번호 수정이 완료되었습니다.");

        if (result.alreadyPracticed) {
            model.addAttribute("point", "이미 계좌 비밀번호 수정 연습을 하였습니다.");
        } else {
            model.addAttribute("point", "포인트가 " + result.point + " 적립되었습니다.");
        }

        return "account/update_pw_ok";
    }

    @GetMapping("/jgig/termination")
    public String termination(@RequestParam("account") long account, HttpSession session, Model model) {
        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";

        AccountDto dto = accountService.getAccount(account);
        Date regdate = dto.getRegdate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String formattedRegdate = dateFormat.format(regdate);
        model.addAttribute("formattedRegdate", formattedRegdate);
        model.addAttribute("dto", dto);
        return "account/termination";
    }

    @PostMapping("/jgig/termination_action")
    public String termination_action(RedirectAttributes redirect,
                                     @RequestParam("account") long account,
                                     @RequestParam("act_password") int pw,
                                     HttpSession session,
                                     Model model) {

        String returnVal = login_check(session);
        if (returnVal.equals("redirect:/jgig/login"))
            return "redirect:/jgig/login";

        String memId = (String) session.getAttribute("mem_id");

        TerminationResult result = accountService.terminateAccount(account, pw, memId);

        if (!result.passwordMatched) {
            redirect.addFlashAttribute("msg", "비밀번호가 틀립니다. 다시한번 확인해주세요.");
            return "redirect:/jgig/termination?account=" + account;
        }

        model.addAttribute("msg", "계좌 해지가 완료되었습니다.");
        if (result.alreadyPracticed) {
            model.addAttribute("point", "이미 계좌 해지 연습을 하였습니다.");
        } else {
            model.addAttribute("point", "포인트가 " + result.point + " 적립되었습니다.");
        }

        return "account/termination_ok";
    }
}
