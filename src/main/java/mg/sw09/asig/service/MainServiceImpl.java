package mg.sw09.asig.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mg.sw09.asig.entity.BoardDto;
import mg.sw09.asig.entity.MemberDto;
import mg.sw09.asig.entity.NoticeDto;
import mg.sw09.asig.entity.PointDto;
import mg.sw09.asig.mapper.MainMapper;

@Service
public class MainServiceImpl implements MainService {

    private final MainMapper mainMapper;

    public MainServiceImpl(MainMapper mainMapper) {
        this.mainMapper = mainMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public MainPageData getMainPageData() {
        List<BoardDto> freeList = mainMapper.free_list();
        List<BoardDto> questionList = mainMapper.question_list();
        List<NoticeDto> noticeList = mainMapper.notice_list();
        List<MemberDto> pointRank = mainMapper.point_rank();

        return new MainPageData(freeList, questionList, noticeList, pointRank);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceData getAttendanceData(String memId) {
        List<PointDto> attList = mainMapper.att_list(memId);
        List<Integer> attYes = new ArrayList<>();

        for (int i = 0; i <= 31; i++) {
            attYes.add(null);
        }

        SimpleDateFormat sdf1 = new SimpleDateFormat("d");

        for (PointDto pointDto : attList) {
            int day = Integer.parseInt(sdf1.format(pointDto.getPoint_date()));
            attYes.set(day, day);
        }

        // 이번 달 마지막 날짜 계산
        LocalDate now = LocalDate.now();
        LocalDate firstDate = now.withDayOfMonth(1);
        LocalDate lastDate = now.withDayOfMonth(firstDate.lengthOfMonth());
        int dayOfMonth = lastDate.getDayOfMonth();

        return new AttendanceData(attYes, dayOfMonth);
    }
}