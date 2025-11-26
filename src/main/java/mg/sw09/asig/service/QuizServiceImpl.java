package mg.sw09.asig.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mg.sw09.asig.entity.QuizDto;
import mg.sw09.asig.mapper.QuizMapper;

@Service
public class QuizServiceImpl implements QuizService {

    private final QuizMapper quizMapper;

    private final int ans = 1; // 정답

    public QuizServiceImpl(QuizMapper quizMapper) {
        this.quizMapper = quizMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public QuizViewData loadQuizData(String memId) throws IOException {
        // 사용자가 오늘 퀴즈를 풀었는지 여부 조회
        QuizDto dto = quizMapper.selectQuiz(memId);

        String quizStat;
        Integer myAnswer = null;
        Integer answer = null;

        if (dto != null) {
            quizStat = dto.getQuiz_stat();
            myAnswer = dto.getMy_answer();
            answer = dto.getAnswer();
        } else {
            quizStat = "F"; // 아직 퀴즈 안 푼 상태
            answer = ans;   // 오늘 정답
        }

        // Jsoup 크롤링
        String URL = "https://m.kbcapital.co.kr/cstmrPtct/fnncInfoSqre/fnncTmng.kbc";
        Document doc = Jsoup.connect(URL)
                .data("targetRow", "127")
                .data("rowSize", "4")
                .get();

        Elements el = doc.select("ul.sp-accord.nospace");

        List<Map<String, String>> resultList = new LinkedList<>();

        for (Element e : el.select("li")) {
            Map<String, String> map = new HashMap<>();
            map.put("tit", e.select("span.tit").text());
            map.put("inner", e.select("div.inner").text());
            resultList.add(map);
        }

        return new QuizViewData(quizStat, myAnswer, answer, resultList);
    }

    @Override
    @Transactional
    public QuizDto submitQuiz(String memId, int selectedOpt) {
        // 정답일 때
        if (selectedOpt == ans) {
            quizMapper.insertQuiz(memId, "Y", ans, selectedOpt);
            quizMapper.insertPoint(memId); // 포인트 insert
            return new QuizDto(memId, "Y", "", selectedOpt, ans);
        }

        // 오답일 때
        quizMapper.insertQuiz(memId, "N", ans, selectedOpt);
        return new QuizDto(memId, "N", "", selectedOpt, ans);
    }
}