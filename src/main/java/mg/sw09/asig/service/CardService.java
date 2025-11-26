package mg.sw09.asig.service;

import java.util.List;

import mg.sw09.asig.entity.CardDto;

public interface CardService {

    CardPwResult changePassword(int cdNo, String newPw, String memId);

    CardStatusResult changeStatus(int cdNo, String memId);

    CardCancellationResult cancelCard(int cdNo, String memId);

    CardDto getCard(int cdNo);

    CardListResult getCardList(String memId, int currentPage);

    CardIssuanceResult issueCard(CardDto cardDto, String memId);

    public class CardPwResult {
        public CardDto card;
        public boolean alreadyPointGiven;
        public int point;

        public CardPwResult(CardDto card, boolean alreadyPointGiven, int point) {
            this.card = card;
            this.alreadyPointGiven = alreadyPointGiven;
            this.point = point;
        }
    }

    public class CardStatusResult {
        public CardDto card;
        public boolean alreadyPointGiven;
        public int point;

        public CardStatusResult(CardDto card, boolean alreadyPointGiven, int point) {
            this.card = card;
            this.alreadyPointGiven = alreadyPointGiven;
            this.point = point;
        }
    }

    public class CardCancellationResult {
        public boolean alreadyPointGiven;
        public int point;

        public CardCancellationResult(boolean alreadyPointGiven, int point) {
            this.alreadyPointGiven = alreadyPointGiven;
            this.point = point;
        }
    }

    public class CardListResult {
        public List<CardDto> list;
        public int currentPage;
        public int startPage;
        public int endPage;
        public int maxPage;

        public CardListResult(List<CardDto> list,
                              int currentPage,
                              int startPage,
                              int endPage,
                              int maxPage) {
            this.list = list;
            this.currentPage = currentPage;
            this.startPage = startPage;
            this.endPage = endPage;
            this.maxPage = maxPage;
        }
    }

    public class CardIssuanceResult {
        public CardDto cardSuccess;
        public boolean alreadyPointGiven;
        public int point;

        public CardIssuanceResult(CardDto cardSuccess, boolean alreadyPointGiven, int point) {
            this.cardSuccess = cardSuccess;
            this.alreadyPointGiven = alreadyPointGiven;
            this.point = point;
        }
    }
}