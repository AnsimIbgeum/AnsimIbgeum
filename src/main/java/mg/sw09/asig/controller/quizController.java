package mg.sw09.asig.controller;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import mg.sw09.asig.entity.QuizDto;
import mg.sw09.asig.service.QuizService;
import mg.sw09.asig.service.QuizService.QuizViewData;

@Controller
public class quizController {

    private final QuizService quizService;

    public quizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/jgig/quiz")
    public String loadQuizData(Model model, HttpSession session) throws IOException {
        // 로그인 체크
        String memId = (String) session.getAttribute("mem_id");
        if (memId == null) {
            return "redirect:/jgig/login";
        }

        QuizViewData data = quizService.loadQuizData(memId);

        model.addAttribute("quiz_stat", data.quizStat);
        model.addAttribute("my_answer", data.myAnswer);
        model.addAttribute("answer", data.answer);
        model.addAttribute("resultList", data.resultList);
        model.addAttribute("ans", data.answer); // 기존처럼 ans도 따로 넘겨줌

        return "quiz/showQuiz";
    }

    @PostMapping("/jgig/submitQuiz")
    @ResponseBody
    public QuizDto submitQuiz(@RequestParam("selectedOpt") int selectedOpt,
                              HttpSession session) {

        String mem_id = (String) session.getAttribute("mem_id");
        if (mem_id == null) {
            // 상황에 따라 예외 던지거나, 에러 정보를 담은 DTO를 리턴하는 식으로 처리
            return new QuizDto(null, "F", "로그인 필요", selectedOpt, 0);
        }

        return quizService.submitQuiz(mem_id, selectedOpt);
    }
}