package mg.sw09.asig.service;

import mg.sw09.asig.entity.MemberDto;

public interface MemberService {

    // 회원가입 (주민번호 두 조각 + DTO)
    void register(MemberDto dto, String ssn1, String ssn2);

    // 로그인: 아이디 + 평문 비밀번호 → 성공 여부 + 회원 정보/에러 메시지
    LoginResult login(String memId, String rawPassword);

    // 회원 상세 정보 조회
    MemberDto getMemberDetail(String memId);

    // 수정 페이지용: 비밀번호는 빈 문자열로 세팅한 DTO 반환
    MemberDto getMemberForUpdate(String memId);

    // 회원 정보 수정 (비번 유지/변경 포함)
    void updateMember(String memId, MemberDto updateDto);

    // 회원 탈퇴 (비밀번호 검증 포함)
    boolean deleteMember(String memId, String rawPassword);

    // ===== 결과 DTO =====
    class LoginResult {
        public final boolean success;
        public final MemberDto member;
        public final String errorMessage;

        public LoginResult(boolean success, MemberDto member, String errorMessage) {
            this.success = success;
            this.member = member;
            this.errorMessage = errorMessage;
        }

        public static LoginResult success(MemberDto member) {
            return new LoginResult(true, member, null);
        }

        public static LoginResult fail(String message) {
            return new LoginResult(false, null, message);
        }
    }
}