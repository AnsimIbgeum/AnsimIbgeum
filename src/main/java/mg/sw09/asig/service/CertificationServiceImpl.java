package mg.sw09.asig.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mg.sw09.asig.mapper.CardMapper;

@Service
public class CertificationServiceImpl implements CertificationService {

    private final CardMapper cardMapper;

    public CertificationServiceImpl(CardMapper cardMapper) {
        this.cardMapper = cardMapper;
    }

    @Override
    @Transactional
    public CertificationResult completeCertification(String memId) {

        int countDailyCheckIn = cardMapper.countDailyCheckIn(memId, "인증서");
        String message;

        if (countDailyCheckIn == 0) {
            cardMapper.checkPoint(memId, 10, "인증서");
            cardMapper.updatePoint(memId, 10); // 멤버 포인트 업데이트
            message = "10 포인트가 적립되었습니다.";
        } else {
            message = "이미 포인트가 지급되었습니다.";
        }

        return new CertificationResult(message);
    }
}