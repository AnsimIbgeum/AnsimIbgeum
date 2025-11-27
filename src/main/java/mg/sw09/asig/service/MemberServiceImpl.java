package mg.sw09.asig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mg.sw09.asig.entity.MemberDto;
import mg.sw09.asig.mapper.MemberMapper;
import mg.sw09.asig.mapper.PointMapper;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final PointMapper pointMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberServiceImpl(MemberMapper memberMapper,
                             PointMapper pointMapper,
                             PasswordEncoder passwordEncoder) {
        this.memberMapper = memberMapper;
        this.pointMapper = pointMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void register(MemberDto dto, String ssn1, String ssn2) {
        String ssn = ssn1 + ssn2;
        dto.setSsn(ssn);

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
    @Transactional(readOnly = true)
    public MemberDto getMemberDetail(String memId) {
        return memberMapper.detail(memId);
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