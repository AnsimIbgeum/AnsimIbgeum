package mg.sw09.asig.service;
import java.util.List;
import mg.sw09.asig.entity.AccountDto;

public interface AccountService {
//    AccountDetailDto getAccountDetail(Long memberId, String accountNo);

    // 회원 본인 확인
    boolean checkMember(String memIdFromSession,
                        String memPhoneFromSession,
                        String memSsnFromSession,
                        String inputName,
                        String inputSsn,
                        String inputPhone);

    // 계좌 개설 (포인트/연습 여부까지 포함한 결과 반환)
    OpenAccountResult openAccount(AccountDto dto, String memId);

    // 계좌 목록 + 페이징 정보를 한 번에 제공
    AccountListResult getAccountList(String memId, int currentPage);

    // 계좌 상세 조회 (계좌관리/해지 폼에서 공통 사용)
    AccountDto getAccount(long account);

    // 비밀번호 수정
    UpdatePasswordResult updatePassword(AccountDto dto, String memId);

    // 계좌 해지
    TerminationResult terminateAccount(long account, int inputPassword, String memId);

    class OpenAccountResult {
        public long accountNum;
        public boolean alreadyPracticed;
        public int point;

        public OpenAccountResult(long accountNum, boolean alreadyPracticed, int point) {
            this.accountNum = accountNum;
            this.alreadyPracticed = alreadyPracticed;
            this.point = point;
        }
    }

    class AccountListResult {
        public int total;
        public int startPage;
        public int endPage;
        public int currentPage;
        public int size;
        public int totalBalance;
        public boolean hasPrevious;
        public boolean hasNext;
        public List<AccountDto> list;

        public AccountListResult(int total, int startPage, int endPage, int currentPage, int size,
                                 int totalBalance, boolean hasPrevious, boolean hasNext, List<AccountDto> list) {
            this.total = total;
            this.startPage = startPage;
            this.endPage = endPage;
            this.currentPage = currentPage;
            this.size = size;
            this.totalBalance = totalBalance;
            this.hasPrevious = hasPrevious;
            this.hasNext = hasNext;
            this.list = list;
        }
    }

    class UpdatePasswordResult {
        public boolean alreadyPracticed;
        public int point;

        public UpdatePasswordResult(boolean alreadyPracticed, int point) {
            this.alreadyPracticed = alreadyPracticed;
            this.point = point;
        }
    }

    class TerminationResult {
        public boolean passwordMatched;
        public boolean alreadyPracticed;
        public int point;

        public TerminationResult(boolean passwordMatched, boolean alreadyPracticed, int point) {
            this.passwordMatched = passwordMatched;
            this.alreadyPracticed = alreadyPracticed;
            this.point = point;
        }
    }
}
