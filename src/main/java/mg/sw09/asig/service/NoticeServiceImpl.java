package mg.sw09.asig.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mg.sw09.asig.entity.Criteria;
import mg.sw09.asig.entity.NoticeDto;
import mg.sw09.asig.mapper.NoticeMapper;

@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Autowired
    public NoticeServiceImpl(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeListResult getNoticeList(Integer pageNum) {

        int total = noticeMapper.getTotal();

        if (pageNum == null) {
            pageNum = 1;
        }

        Criteria criteria = new Criteria(pageNum, total);
        List<NoticeDto> list = noticeMapper.listWithPaging(criteria);

        return new NoticeListResult(total, list, criteria);
    }

    @Override
    @Transactional
    public NoticeDetailResult getNoticeDetail(int no) {

        NoticeDto dto = noticeMapper.findByNo(no);
        if (dto != null) {
            noticeMapper.updateView(dto);
        }

        return new NoticeDetailResult(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeSearchResult searchNotice(String searchTag, String keyword, Integer pageNum) {

        if (pageNum == null) {
            pageNum = 1;
        }

        Criteria criteria;
        List<NoticeDto> list;
        int total;

        if ("제목".equals(searchTag)) {
            total = noticeMapper.getSearchTotalByTitle(keyword);
            criteria = new Criteria(pageNum, searchTag, keyword, total);
            list = noticeMapper.searchListWithPagingByTitle(criteria);
        } else {
            total = noticeMapper.getSearchTotalByMem(keyword);
            criteria = new Criteria(pageNum, searchTag, keyword, total);
            list = noticeMapper.searchListWithPagingByMem(criteria);
        }

        return new NoticeSearchResult(total, list, criteria);
    }
}