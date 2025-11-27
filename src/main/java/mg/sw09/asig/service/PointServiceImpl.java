package mg.sw09.asig.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mg.sw09.asig.entity.MemberDto;
import mg.sw09.asig.entity.PointDto;
import mg.sw09.asig.mapper.MemberMapper;
import mg.sw09.asig.mapper.PointMapper;

@Service
public class PointServiceImpl implements PointService {

    private final PointMapper pointMapper;
    private final MemberMapper memberMapper;

    public PointServiceImpl(PointMapper pointMapper, MemberMapper memberMapper) {
        this.pointMapper = pointMapper;
        this.memberMapper = memberMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalPoint(String memId) {
        if (memId == null) return 0;
        List<PointDto> pointList = pointMapper.getPointByMemberId(memId);
        return calculateTotalPoint(pointList);
    }

    @Override
    @Transactional(readOnly = true)
    public String getMemberName(String memId) {
        if (memId == null) return "";
        MemberDto member = memberMapper.detail(memId);
        return member != null ? member.getMem_nm() : "";
    }

    @Override
    @Transactional(readOnly = true)
    public FilterResult getFilteredPoints(String memId, String filter, int currentPage) {

        int size = 5;
        int total = pointMapper.getFilteredPointByMemberId(memId, filter);

        int totalPages = total / size;
        if (total % size > 0) {
            totalPages++;
        }

        int startPage = currentPage / size * size + 1;
        if (currentPage % size == 0) {
            startPage -= size;
        }

        int endPage = startPage + (size - 1);
        if (endPage > totalPages) {
            endPage = totalPages;
        }

        boolean hasPrevious = currentPage > 1;
        boolean hasNext = currentPage < totalPages;

        List<PointDto> pointList = pointMapper.listWithPaging(
                memId, filter,
                currentPage * size - size + 1,
                currentPage * size
        );

        return new FilterResult(
                total,
                startPage,
                endPage,
                currentPage,
                size,
                hasPrevious,
                hasNext,
                pointList,
                filter
        );
    }

    @Override
    @Transactional
    public ConversionResult convertPoints(String memId, int conversionPoint) {
        if (memId == null) {
            return ConversionResult.error("로그인이 필요합니다.");
        }

        int totalPoint = getTotalPoint(memId);
        int remainingPoint = totalPoint - conversionPoint;

        if (remainingPoint < 0) {
            return ConversionResult.error("보유 포인트보다 많은 포인트를 전환할 수 없습니다.");
        }

        // 포인트 차감 (음수 값으로 저장)
        pointMapper.conversionPoint(memId, -conversionPoint);

        return new ConversionResult(true, totalPoint, remainingPoint, null);
    }

    @Override
    @Transactional
    public CheckinResult checkIn(String memId) {
        if (memId == null) {
            return new CheckinResult(null, true, 0);
        }

        MemberDto member = memberMapper.detail(memId);

        int count = pointMapper.countDailyCheckIn(memId);
        if (count >= 1) {
            // 이미 출석체크 함
            return new CheckinResult(member, true, 0);
        }

        PointDto point = new PointDto();
        point.setPoint(10);
        point.setMem_id(memId);

        // 포인트 테이블 insert
        pointMapper.checkPoint(memId, point.getPoint());
        // 멤버 포인트 업데이트
        pointMapper.updatePoint(memId, 10);

        return new CheckinResult(member, false, 10);
    }

    // ===== 내부 유틸 =====
    private int calculateTotalPoint(List<PointDto> pointList) {
        int totalPoint = 0;
        for (PointDto point : pointList) {
            totalPoint += point.getPoint();
        }
        return totalPoint;
    }
}