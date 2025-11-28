package mg.sw09.asig.service;
import java.time.LocalDate;
import java.util.List;

import mg.sw09.asig.util.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mg.sw09.asig.entity.AccountDto;
import mg.sw09.asig.mapper.AccountMapper;

@Service
public class AccountServiceImpl implements AccountService{
    private final AccountMapper accountMapper;
    private final int POINT = 5;
    private final AESUtil aesUtil;

    @Autowired
    public AccountServiceImpl(AccountMapper accountMapper, AESUtil aesUtil) {
        this.accountMapper = accountMapper;
        this.aesUtil = aesUtil;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkMember(String memIdFromSession,
                               String memPhoneFromSession,
                               String memSsnFromSession,
                               String inputName,
                               String inputSsn,
                               String inputPhone) {

        if (memIdFromSession == null) {
            return false;
        }

        inputSsn = aesUtil.encrypt(inputSsn);
        String memNameFromDb = accountMapper.findById(memIdFromSession);

        return inputName.equals(memNameFromDb)
                && memSsnFromSession.equals(inputSsn)
                && memPhoneFromSession.equals(inputPhone);
    }

    @Override
    @Transactional
    public OpenAccountResult openAccount(AccountDto dto, String memId) {

        accountMapper.insert(dto);
        long accountNum = accountMapper.account_num(dto);

        LocalDate nowDate = LocalDate.now();
        String nowDateString = nowDate.toString();

        int checkPractice = accountMapper.check_practice(nowDateString, memId, "계좌개설");

        if (checkPractice > 0) {
            // 이미 연습한 경우: 포인트 적립 X
            return new OpenAccountResult(accountNum, true, 0);
        }

        // 처음 연습: 포인트 적립
        accountMapper.setPoint(POINT, "계좌개설", memId);

        return new OpenAccountResult(accountNum, false, POINT);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountListResult getAccountList(String memId, int currentPage) {

        int size = 3;
        int pagingCount = 2;

        int total = accountMapper.list(memId);

        int totalPages = total / size;
        if (total % size > 0) {
            totalPages++;
        }

        int startPage = currentPage / pagingCount * pagingCount + 1;
        if (currentPage % pagingCount == 0) {
            startPage -= pagingCount;
        }

        int endPage = startPage + (pagingCount - 1);
        if (endPage > totalPages) {
            endPage = totalPages;
        }

        List<AccountDto> list =
                accountMapper.listWithPaging(memId, currentPage * size - size + 1, currentPage * size);

        int totalBalance = 0;
        if (!list.isEmpty()) {
            totalBalance = accountMapper.totalBalance(memId);
        }

        boolean hasPrevious = startPage > 1;
        boolean hasNext = endPage < totalPages;

        return new AccountListResult(
                total, startPage, endPage, currentPage, size,
                totalBalance, hasPrevious, hasNext, list
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccount(long account) {
        return accountMapper.findByAccount(account);
    }

    @Override
    @Transactional
    public UpdatePasswordResult updatePassword(AccountDto dto, String memId) {

        accountMapper.update(dto);

        LocalDate nowDate = LocalDate.now();
        String nowDateString = nowDate.toString();
        int checkPractice = accountMapper.check_practice(nowDateString, memId, "비번수정");

        if (checkPractice > 0) {
            return new UpdatePasswordResult(true, 0);
        }

        accountMapper.setPoint(POINT, "비번수정", memId);
        return new UpdatePasswordResult(false, POINT);
    }

    @Override
    @Transactional
    public TerminationResult terminateAccount(long account, int inputPassword, String memId) {

        int checkPw = accountMapper.checkPw(account);
        LocalDate nowDate = LocalDate.now();
        String nowDateString = nowDate.toString();
        int checkPractice = accountMapper.check_practice(nowDateString, memId, "계좌해지");

        if (checkPw != inputPassword) {
            return new TerminationResult(false, false, 0);
        }

        AccountDto dto = new AccountDto();
        dto.setAccount(account);
        accountMapper.terminate(dto);

        if (checkPractice > 0) {
            return new TerminationResult(true, true, 0);
        }

        accountMapper.setPoint(POINT, "계좌해지", memId);
        return new TerminationResult(true, false, POINT);
    }
}
