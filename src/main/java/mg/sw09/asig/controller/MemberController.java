package mg.sw09.asig.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import mg.sw09.asig.entity.MemberDto;
import mg.sw09.asig.mapper.MemberMapper;
import mg.sw09.asig.mapper.PointMapper;

@Controller
public class MemberController {
    @Autowired
    private MemberMapper memberMapper;
    
    @Autowired
    private PointMapper pointMapper;

    @Autowired
    private PasswordEncoder passwordEncoder; //비밀번호 해싱 기능 인터페이스

    @GetMapping("/jgig/register")
    public String toSignupPage() { // 회원가입 페이지
        return "member/register";
    }

    @PostMapping("/jgig/register")
    public String signup(MemberDto dto, @RequestParam("ssn_1") String ssn_1, @RequestParam("ssn_2") String ssn_2,Model model) { // 회원가입
        try {
            String ssn = ssn_1 + ssn_2;
            dto.setSsn(ssn);

            //비밀번호 암호화하여 저장
            String encodedPassword = passwordEncoder.encode(dto.getMem_pw());
            dto.setMem_pw(encodedPassword);

            memberMapper.signup(dto);
            model.addAttribute("successMessage", "회원가입에 성공하였습니다.");

        } catch (Exception e) {
            e.printStackTrace();
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
    public String login(MemberDto dto, HttpSession session, Model model, HttpServletResponse response,  @RequestParam(value = "remember_me", required = false) String rememberMe) { // 로그인
        //아이디로 회원 조회
        MemberDto loginDto = memberMapper.detail(dto.getMem_id());

        //loginDTO가 존재하고 비밀번호가 일치하면 로그인 성공
        //기존 로직은 SQL에서 비번 검사하기에 BCrypt로는 검사 불가능
        if (loginDto != null) {
            // 로그인 성공 로직
            session.setAttribute("loggedIn", true);
            String id = loginDto.getMem_id();
            session.setAttribute("mem_id", id);
            String ssn = loginDto.getSsn();
            session.setAttribute("ssn", ssn);
            String mem_nm = loginDto.getMem_nm();
            session.setAttribute("mem_nm", mem_nm);
            String phone_num = loginDto.getPhone_num();
            session.setAttribute("phone_num", phone_num);

            //주민등록번호, 이름, 전화번호
            model.addAttribute("memberDto", loginDto);

            //로그인 유지 체크박스가 선택되었을 때
            if (rememberMe != null && rememberMe.equals("on")) {
                //"remember_me" 쿠키 생성 및 설정 (예: 7일 동안 유지)
                Cookie rememberMeCookie = new Cookie("remember_me", "true");
                rememberMeCookie.setMaxAge(7 * 24 * 60 * 60);
                response.addCookie(rememberMeCookie);
            }
            return "redirect:/jgig/";
        } else {
            //로그인 실패 (회원이 없거나 비밀번호가 틀림)
            String errorMessage = "아이디나 비밀번호를 다시 확인해주세요.";
            model.addAttribute("loginError", errorMessage);
            return "login/login_form";
        }
    }

//    @GetMapping("/jgig/main")
//    public String loadmain() {
//        return "login/login_main";
//
//    }
    @GetMapping("/jgig/logout")
    public String logout(HttpSession session) { // 로그아웃
        session.invalidate();
        return "redirect:/jgig/";
    }

//    @PostMapping("/jgig/logout")
//    public String logout(HttpSession session) { // 로그아웃
//        session.invalidate();
//        return "redirect:/jgig/login";
//    }

//    @GetMapping("/jgig/member_detail")
//    public String todetailPage(@RequestParam("mem_id") , HttpSession session, Model model) { // 회원 정보 수정 페이지
//        String mem_id = (String) session.getAttribute("mem_id");
//        if (mem_id != null) { // 로그인된 상태
//            model.addAttribute("memberDto", mem_id);
//            return "member/detail";
//        }
//        return "redirect:/jgig/login";
//    }
    @GetMapping("/jgig/member_detail") // 회원 상세 정보
    public String detail(@RequestParam("mem_id") String mem_id, HttpSession session, Model model) {
        MemberDto infoDto = memberMapper.detail(mem_id);
        if (infoDto != null) { // 로그인된 상태
            model.addAttribute("memberDto", infoDto);
            return "member/detail";
        }
        return "redirect:/jgig/login";
    }
    
//    @PostMapping("/jgig/member_detail") // 회원 상세 정보
//    public String detail(@RequestParam("mem_id") String mem_id, HttpSession session, Model model) {
//        MemberDto infoDto = memberMapper.detail(mem_id);
//        if (infoDto != null) { // 로그인된 상태
//            model.addAttribute("memberDto", infoDto);
//            return "member/detail";
//        }
//        return "redirect:/jgig/login";
//    }

    @GetMapping("/jgig/member_update")
    public String toUpdatePage(MemberDto dto, HttpSession session, Model model) { // 회원 정보 수정 페이지
        String mem_id = (String) session.getAttribute("mem_id");
        MemberDto update_dto = memberMapper.detail(mem_id);

        //비밀번호는 수정 페이지에 노출되지 않도록 수정
        update_dto.setMem_pw("");

        model.addAttribute("memberDto", update_dto);
        return "member/update";
    }

    @PostMapping("/jgig/member_update")
    public String Update(MemberDto dto, HttpSession session, Model model) { // 회원 정보 수정
        String mem_id = (String) session.getAttribute("mem_id");
        dto.setMem_id(mem_id);

        if (dto.getMem_pw() == null || dto.getMem_pw().trim().isEmpty()) {
            //비밀번호 입력 X -> DB에서 기존 비밀번호 가져와서 설정 (비밀번호 유지)
            MemberDto origin = memberMapper.detail(mem_id);
            dto.setMem_pw(origin.getMem_pw());
        } else {
            //새 비밀번호 입력 -> 새 비밀번호 암호화 후, 설정
            String encodedPw = passwordEncoder.encode(dto.getMem_pw());
            dto.setMem_pw(encodedPw);
        }

        memberMapper.update(dto);
        return "member/detail";
    }

    @GetMapping("/jgig/member_delete")
    public String todeletePage(MemberDto dto, HttpSession session, Model model) { // 회원 페이지
        String mem_id = (String) session.getAttribute("mem_id");
        MemberDto update_dto = memberMapper.detail(mem_id);
        model.addAttribute("memberDto", update_dto);
        return "member/delete";
    }

    @PostMapping("/jgig/member_delete")
    public String delete(
        HttpSession session,
        @RequestParam("mem_pw") String inputPassword,
        RedirectAttributes redirectAttributes) {
        String mem_id = (String) session.getAttribute("mem_id");

        if (mem_id != null) {
            String savedPassword = memberMapper.getPassword(mem_id);

            //passwordEncoder로 암호화된 비밀번호 비교하도록 수정
            if (savedPassword != null && passwordEncoder.matches(inputPassword, savedPassword)) {
                //비밀번호가 일치하는 경우 회원 탈퇴 처리
                memberMapper.delete(mem_id);
                session.invalidate();
                return "redirect:/jgig/login";
            } else {
                //비밀번호가 일치하지 않는 경우 에러 메시지를 리다이렉트로 전달
                session.setAttribute("error", "비밀번호가 일치하지 않습니다. 다시 입력해주세요.");
                return "redirect:/jgig/member_delete"; // 비밀번호 다시 입력 페이지로 리다이렉트
                }
            } else {
            // 세션에 회원 ID가 없는 경우 로그인 페이지로 이동
            return "redirect:/jgig/login";
        }
    }
}