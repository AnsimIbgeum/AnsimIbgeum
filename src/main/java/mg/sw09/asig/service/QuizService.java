package mg.sw09.asig.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import mg.sw09.asig.entity.QuizDto;

public interface QuizService {

    // 퀴즈 페이지 로딩
    QuizViewData loadQuizData(String memId) throws IOException;

    // 퀴즈 제출 처리
    QuizDto submitQuiz(String memId, int selectedOpt);

    // ===== DTO =====
    class QuizViewData {
        public final String quizStat;
        public final Integer myAnswer;
        public final Integer answer;
        public final List<Map<String, String>> resultList;

        public QuizViewData(String quizStat,
                            Integer myAnswer,
                            Integer answer,
                            List<Map<String, String>> resultList) {
            this.quizStat = quizStat;
            this.myAnswer = myAnswer;
            this.answer = answer;
            this.resultList = resultList;
        }
    }
}