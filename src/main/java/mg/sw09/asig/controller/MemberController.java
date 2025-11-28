package mg.sw09.asig.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import mg.sw09.asig.entity.MemberDto;
import mg.sw09.asig.service.MemberService;
import mg.sw09.asig.service.MemberService.LoginResult;

@Controller
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/jgig/register")
    public String toSignupPage() { // 회원가입 페이지
        return "member/register";
    }

    @PostMapping("/jgig/register")
    public String signup(@Valid MemberDto dto,
                         BindingResult bindingResult,
                         @RequestParam("ssn_1") String ssn_1,
                         @RequestParam("ssn_2") String ssn_2,
                         Model model,
                         RedirectAttributes redirectAttributes) { // 회원가입

        //유효성 검사 실패 시, 로직
        if (bindingResult.hasErrors()) {
            // 에러가 있다면 다시 회원가입 폼으로 리턴

            // 입력했던 값 유지 (비밀번호 제외)
            dto.setMem_pw("");
            model.addAttribute("memberDto", dto);

            // 첫 번째 에러 메시지를 모델에 담아서 alert 띄우기
            String firstErrorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            model.addAttribute("valid_msg", firstErrorMessage);

            return "member/register";
        }

        try {
            memberService.register(dto, ssn_1, ssn_2);
            redirectAttributes.addFlashAttribute("successMessage", "회원가입에 성공하였습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "회원가입 중 오류가 발생했습니다.");
        }

        return "redirect:/jgig/login";
    }

    @GetMapping("/jgig/login")
    public String toLoginPage(HttpSession session) { // 로그인 페이지
        String mem_id = (String) session.getAttribute("mem_id");
        if (mem_id != null) { // 로그인된 상태
            return "index";
        }
        return "login/login_form"; // 로그인되지 않은 상태
    }

    @PostMapping("/jgig/login")
    public String login(MemberDto dto,
                        HttpSession session,
                        Model model,
                        HttpServletResponse response,
                        @RequestParam(value = "remember_me", required = false) String rememberMe) { // 로그인

        LoginResult result = memberService.login(dto.getMem_id(), dto.getMem_pw());

        if (result.success) {
            MemberDto loginDto = result.member;

            session.setAttribute("loggedIn", true);
            session.setAttribute("mem_id", loginDto.getMem_id());
            session.setAttribute("ssn", loginDto.getSsn());
            session.setAttribute("mem_nm", loginDto.getMem_nm());
            session.setAttribute("phone_num", loginDto.getPhone_num());

            // 로그인 유지 체크박스 선택 시 쿠키 설정
            if ("on".equals(rememberMe)) {
                Cookie rememberMeCookie = new Cookie("remember_me", "true");
                rememberMeCookie.setMaxAge(7 * 24 * 60 * 60);
                response.addCookie(rememberMeCookie);
            }

            return "redirect:/jgig/";
        } else {
            model.addAttribute("loginError", result.errorMessage);
            return "login/login_form";
        }
    }

    @GetMapping("/jgig/logout")
    public String logout(HttpSession session) { // 로그아웃
        session.invalidate();
        return "redirect:/jgig/";
    }

    @GetMapping("/jgig/member_detail") // 회원 상세 정보
    public String detail(@RequestParam("mem_id") String mem_id,
                         HttpSession session,
                         Model model) {

        MemberDto infoDto = memberService.getMemberDetail(mem_id);
        if (infoDto != null) {
            model.addAttribute("memberDto", infoDto);
            return "member/detail";
        }
        return "redirect:/jgig/login";
    }

    @GetMapping("/jgig/member_update")
    public String toUpdatePage(MemberDto dto,
                               HttpSession session,
                               Model model) { // 회원 정보 수정 페이지

        String mem_id = (String) session.getAttribute("mem_id");
        if (mem_id == null) {
            return "redirect:/jgig/login";
        }

        MemberDto updateDto = memberService.getMemberForUpdate(mem_id);
        model.addAttribute("memberDto", updateDto);
        return "member/update";
    }

    @PostMapping("/jgig/member_update")
    public String update(MemberDto dto,
                         HttpSession session,
                         Model model) { // 회원 정보 수정

        String mem_id = (String) session.getAttribute("mem_id");
        if (mem_id == null) {
            return "redirect:/jgig/login";
        }

        memberService.updateMember(mem_id, dto);
        // 수정 후에는 다시 상세 페이지로
        return "redirect:/jgig/member_detail?mem_id=" + mem_id;
    }

    @GetMapping("/jgig/member_delete")
    public String toDeletePage(MemberDto dto,
                               HttpSession session,
                               Model model) {

        String mem_id = (String) session.getAttribute("mem_id");
        if (mem_id == null) {
            return "redirect:/jgig/login";
        }

        MemberDto infoDto = memberService.getMemberDetail(mem_id);
        model.addAttribute("memberDto", infoDto);
        return "member/delete";
    }

    @PostMapping("/jgig/member_delete")
    public String delete(HttpSession session,
                         @RequestParam("mem_pw") String inputPassword,
                         RedirectAttributes redirectAttributes) {

        String mem_id = (String) session.getAttribute("mem_id");

        if (mem_id == null) {
            return "redirect:/jgig/login";
        }

        boolean deleted = memberService.deleteMember(mem_id, inputPassword);

        if (deleted) {
            session.invalidate();
            return "redirect:/jgig/login";
        } else {
            session.setAttribute("error", "비밀번호가 일치하지 않습니다. 다시 입력해주세요.");
            return "redirect:/jgig/member_delete";
        }
    }
}