package mg.sw09.asig.service;

import lombok.RequiredArgsConstructor;
import mg.sw09.asig.entity.MemberDto;
import mg.sw09.asig.mapper.MemberMapper;
import mg.sw09.asig.util.AESUtil;
import mg.sw09.asig.util.MaskingUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final AESUtil aesUtil;

    @Override
    @Transactional
    public void register(MemberDto dto, String ssn1, String ssn2) {
        String ssn = ssn1 + "-" + ssn2;

        //주민번호: AES 암호화
        String encryptedSSN = aesUtil.encrypt(ssn);
        dto.setSsn(encryptedSSN);

        //비밀번호: 단방향 해시 암호화
        String encodedPassword = passwordEncoder.encode(dto.getMem_pw());
        dto.setMem_pw(encodedPassword);

        memberMapper.signup(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResult login(String memId, String rawPassword) {
        MemberDto loginDto = memberMapper.detail(memId);

        if (loginDto != null && passwordEncoder.matches(rawPassword, loginDto.getMem_pw())) {
            return LoginResult.success(loginDto);
        } else {
            return LoginResult.fail("아이디나 비밀번호를 다시 확인해주세요.");
        }
    }

    @Override
    public MemberDto getMemberDetail(String mem_id) {
        MemberDto member = memberMapper.detail(mem_id);
        if (member != null) {
            //주민등록번호 복호화
            String decryptedSSN = aesUtil.decrypt(member.getSsn());

            //주민등록번호 마스킹 처리
            String maskedSSN = MaskingUtil.maskSSN(decryptedSSN);
            member.setSsn(maskedSSN);

            //비밀번호 제거 (보안상 화면에 노출 X)
            member.setMem_pw("");
        }
        return member;
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDto getMemberForUpdate(String memId) {
        MemberDto dto = memberMapper.detail(memId);
        if (dto != null) {
            // 비밀번호는 화면에 노출하지 않도록 비움
            dto.setMem_pw("");
        }
        return dto;
    }

    @Override
    @Transactional
    public void updateMember(String memId, MemberDto updateDto) {
        updateDto.setMem_id(memId);

        if (updateDto.getMem_pw() == null || updateDto.getMem_pw().trim().isEmpty()) {
            // 비밀번호 입력 X → 기존 비번 그대로 유지
            MemberDto origin = memberMapper.detail(memId);
            if (origin != null) {
                updateDto.setMem_pw(origin.getMem_pw());
            }
        } else {
            // 새 비밀번호 입력 → 암호화 후 저장
            String encodedPw = passwordEncoder.encode(updateDto.getMem_pw());
            updateDto.setMem_pw(encodedPw);
        }

        memberMapper.update(updateDto);
    }

    @Override
    @Transactional
    public boolean deleteMember(String memId, String rawPassword) {
        String savedPassword = memberMapper.getPassword(memId);
        if (savedPassword != null && passwordEncoder.matches(rawPassword, savedPassword)) {
            memberMapper.delete(memId);
            return true;
        }
        return false;
    }
}