package mg.sw09.asig.service;

import java.util.List;

import mg.sw09.asig.entity.AccountDto;
import mg.sw09.asig.entity.TransferDto;

public interface TransferService {

    // 이체 폼용 계좌 정보 조회
    AccountDto getAccountForTransfer(long account);

    // 계좌 비밀번호 검증
    boolean checkAccountPassword(long account, int inputPassword);

    // 실제 계좌 이체 수행
    TransferResult executeTransfer(TransferDto transferDto, String memId);

    // 거래내역 조회 폼용(사용자 계좌 목록)
    List<TransferDto> getAccountList(String memId);

    // 월별 거래내역 조회
    HistoryResult getMonthlyHistory(long selectedAccount, int currentPage,
                                    int year, int month, String memId, String practiceType);

    // 기간(달력) 거래내역 조회
    HistoryResult getCalendarHistory(long selectedAccount, int currentPage,
                                     String startDate, String endDate, String memId, String practiceType);

    // ====== 결과 DTO들 ======

    class TransferResult {
        public final boolean success;
        public final String msg;
        public final String pointMessage;
        public final TransferDto dto;

        public TransferResult(boolean success, String msg, String pointMessage, TransferDto dto) {
            this.success = success;
            this.msg = msg;
            this.pointMessage = pointMessage;
            this.dto = dto;
        }

        public static TransferResult success(String msg, String pointMessage, TransferDto dto) {
            return new TransferResult(true, msg, pointMessage, dto);
        }

        public static TransferResult fail(String msg) {
            return new TransferResult(false, msg, null, null);
        }
    }

    class HistoryResult {
        public final int total;
        public final int startPage;
        public final int endPage;
        public final int currentPage;
        public final int size;
        public final boolean hasPrevious;
        public final boolean hasNext;
        public final java.util.List<TransferDto> transferList;

        public HistoryResult(int total, int startPage, int endPage, int currentPage,
                             int size, boolean hasPrevious, boolean hasNext,
                             java.util.List<TransferDto> transferList) {
            this.total = total;
            this.startPage = startPage;
            this.endPage = endPage;
            this.currentPage = currentPage;
            this.size = size;
            this.hasPrevious = hasPrevious;
            this.hasNext = hasNext;
            this.transferList = transferList;
        }
    }
}