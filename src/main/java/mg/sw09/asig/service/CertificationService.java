package mg.sw09.asig.service;

public interface CertificationService {

    // 인증서 발급 시 포인트 적립/중복 여부 처리
    CertificationResult completeCertification(String memId);

    // 결과를 담을 작은 DTO
    class CertificationResult {
        public final String pointMessage;

        public CertificationResult(String pointMessage) {
            this.pointMessage = pointMessage;
        }
    }
}