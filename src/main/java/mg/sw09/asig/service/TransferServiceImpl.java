package mg.sw09.asig.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mg.sw09.asig.entity.AccountDto;
import mg.sw09.asig.entity.TransferDto;
import mg.sw09.asig.mapper.TransferMapper;
import mg.sw09.asig.service.TransferService.HistoryResult;
import mg.sw09.asig.service.TransferService.TransferResult;

@Service
public class TransferServiceImpl implements TransferService {

    private final TransferMapper transferMapper;
    private final int point = 5;

    @Autowired
    public TransferServiceImpl(TransferMapper transferMapper) {
        this.transferMapper = transferMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccountForTransfer(long account) {
        return transferMapper.findByAccount(account);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkAccountPassword(long account, int inputPassword) {
        int realPw = transferMapper.findByActPassword(account);
        return realPw == inputPassword;
    }

    @Override
    @Transactional
    public TransferResult executeTransfer(TransferDto transferDto, String memId) {
        int balance = transferMapper.findByBalance(transferDto.getAccount());

        LocalDate nowDate = LocalDate.now();
        String nowDateString = nowDate.toString();
        int check_practice = transferMapper.check_practice(nowDateString, memId, "계좌이체");

        // 잔액 부족 체크
        if (balance == 0 || transferDto.getDepo_mon() > balance) {
            return TransferResult.fail("잔액이 부족합니다.");
        }

        // 이체 로직
        transferMapper.insert(transferDto);
        transferMapper.update(transferDto); // 보낸 사람 계좌에서 출금

        int update_receive_mon = transferMapper.update_receive_mon(transferDto);
        if (update_receive_mon != 0) {
            transferMapper.receive(transferDto); // 받는 사람 계좌 입금
        }

        String msg = "이체가 완료되었습니다.";
        String pointMessage;

        if (check_practice > 0) {
            pointMessage = "이미 계좌 이체 연습을 하였습니다.";
        } else {
            transferMapper.setPoint(point, "계좌이체", memId); // 매개변수 점수, 연습종류, mem_id
            pointMessage = "포인트가 " + point + " 적립되었습니다.";
        }

        return TransferResult.success(msg, pointMessage, transferDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferDto> getAccountList(String memId) {
        return transferMapper.accountList(memId);
    }

    @Override
    @Transactional
    public HistoryResult getMonthlyHistory(long selectedAccount, int currentPage,
                                           int year, int month, String memId, String practiceType) {

        // 월 문자열 구성 (기존 로직 그대로 유지)
        String input_month = "0";
        if (month >= 0 && month <= 9) {
            input_month += month;
        } else {
            input_month = String.valueOf(month);
        }
        String yearMon = year + "/" + input_month;

        int total = transferMapper.list(selectedAccount, yearMon);

        int size = 5;
        int pagingCount = 2;
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

        List<TransferDto> transferList = transferMapper.listWithPaging(
                selectedAccount,
                yearMon,
                currentPage * size - size + 1,
                currentPage * size
        );

        boolean hasPrevious = startPage > 1;
        boolean hasNext = endPage < totalPages;

        // 포인트 처리
        LocalDate nowDate = LocalDate.now();
        String nowDateString = nowDate.toString();
        int check_practice = transferMapper.check_practice(nowDateString, memId, practiceType);

        if (check_practice == 0) {
            transferMapper.setPoint(point, practiceType, memId);
        }

        return new HistoryResult(
                total,
                startPage,
                endPage,
                currentPage,
                size,
                hasPrevious,
                hasNext,
                transferList
        );
    }

    @Override
    @Transactional
    public HistoryResult getCalendarHistory(long selectedAccount, int currentPage,
                                            String startDate, String endDate,
                                            String memId, String practiceType) {

        int total = transferMapper.listCalendar(startDate, endDate, selectedAccount);

        int size = 5;
        int pagingCount = 2;
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

        List<TransferDto> transferList = transferMapper.listCalendarWithPaging(
                startDate,
                endDate,
                selectedAccount,
                currentPage * size - size + 1,
                currentPage * size
        );

        boolean hasPrevious = startPage > 1;
        boolean hasNext = endPage < totalPages;

        // 포인트 처리
        LocalDate nowDate = LocalDate.now();
        String nowDateString = nowDate.toString();
        int check_practice = transferMapper.check_practice(nowDateString, memId, practiceType);

        if (check_practice == 0) {
            transferMapper.setPoint(point, practiceType, memId);
        }

        return new HistoryResult(
                total,
                startPage,
                endPage,
                currentPage,
                size,
                hasPrevious,
                hasNext,
                transferList
        );
    }
}