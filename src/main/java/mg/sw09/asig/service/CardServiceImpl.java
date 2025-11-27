package mg.sw09.asig.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mg.sw09.asig.entity.CardDto;
import mg.sw09.asig.mapper.CardMapper;

@Service
public class CardServiceImpl implements CardService {

    private final CardMapper cardMapper;
    private static final int POINT = 10;

    @Autowired
    public CardServiceImpl(CardMapper cardMapper) {
        this.cardMapper = cardMapper;
    }

    @Override
    @Transactional
    public CardPwResult changePassword(int cdNo, String newPw, String memId) {

        // 비밀번호 변경
        cardMapper.update_pw(cdNo, newPw);
        CardDto updateCard = cardMapper.select_card(cdNo);

        // 포인트 지급 여부 확인
        int countDailyCheckIn = cardMapper.countDailyCheckIn(memId, "카드PW");
        boolean already = countDailyCheckIn > 0;

        if (!already) {
            cardMapper.checkPoint(memId, POINT, "카드PW");
            cardMapper.updatePoint(memId, POINT);
            return new CardPwResult(updateCard, false, POINT);
        }

        return new CardPwResult(updateCard, true, 0);
    }

    @Override
    @Transactional
    public CardStatusResult changeStatus(int cdNo, String memId) {

        cardMapper.update_status(cdNo);
        CardDto updateCard = cardMapper.select_card(cdNo);

        int countDailyCheckIn = cardMapper.countDailyCheckIn(memId, "카드ST");
        boolean already = countDailyCheckIn > 0;

        if (!already) {
            cardMapper.checkPoint(memId, POINT, "카드ST");
            cardMapper.updatePoint(memId, POINT);
            return new CardStatusResult(updateCard, false, POINT);
        }

        return new CardStatusResult(updateCard, true, 0);
    }

    @Override
    @Transactional
    public CardCancellationResult cancelCard(int cdNo, String memId) {

        cardMapper.delete(cdNo);

        int countDailyCheckIn = cardMapper.countDailyCheckIn(memId, "카드해지");
        boolean already = countDailyCheckIn > 0;

        if (!already) {
            cardMapper.checkPoint(memId, POINT, "카드해지");
            cardMapper.updatePoint(memId, POINT);
            return new CardCancellationResult(false, POINT);
        }

        return new CardCancellationResult(true, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public CardDto getCard(int cdNo) {
        return cardMapper.select_card(cdNo);
    }

    @Override
    @Transactional(readOnly = true)
    public CardListResult getCardList(String memId, int currentPage) {

        List<CardDto> cardList = cardMapper.list(memId);
        int cnt = cardList.size();

        int pageSize = 5;
        int maxPage = (int) Math.ceil((double) cnt / pageSize);
        int blockLimit = 5;

        int startRow = (currentPage - 1) * pageSize + 1;
        int endRow = startRow + pageSize - 1;

        int startPage = ((int) Math.ceil((double) currentPage / blockLimit) - 1) * blockLimit + 1;
        int endPage = startPage + blockLimit - 1;
        if (endPage > maxPage) {
            endPage = maxPage;
        }

        List<CardDto> listPaging = cardMapper.list_paging(memId, startRow, endRow);

        return new CardListResult(listPaging, currentPage, startPage, endPage, maxPage);
    }

    @Override
    @Transactional
    public CardIssuanceResult issueCard(CardDto cardDto, String memId) {

        // DB insert
        cardMapper.insert(cardDto);
        // 방금 발급된 카드 조회
        CardDto cardSuccess = cardMapper.find_last(memId);

        int countDailyCheckIn = cardMapper.countDailyCheckIn(memId, "카드발급");
        boolean already = countDailyCheckIn > 0;

        if (!already) {
            cardMapper.checkPoint(memId, POINT, "카드발급");
            cardMapper.updatePoint(memId, POINT);
            return new CardIssuanceResult(cardSuccess, false, POINT);
        }

        return new CardIssuanceResult(cardSuccess, true, 0);
    }
}