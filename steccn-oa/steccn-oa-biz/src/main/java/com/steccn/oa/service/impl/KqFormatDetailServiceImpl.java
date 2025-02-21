package com.steccn.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.vo.QtChartPieValueVO;
import com.steccn.oa.domain.KqFormatDetail;
import com.steccn.oa.service.KqFormatDetailService;
import com.steccn.oa.mapper.KqFormatDetailMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
* @author Admin
* @description 针对表【kq_format_detail】的数据库操作Service实现
* @createDate 2023-11-09 17:25:16
*/
@Service
public class KqFormatDetailServiceImpl extends ServiceImpl<KqFormatDetailMapper, KqFormatDetail>
    implements KqFormatDetailService{

    @Override
    public Integer kqNum() {
        KqFormatDetail one = getOne(new QueryWrapper<KqFormatDetail>().select("COUNT(DISTINCT resourceId) AS resourceId")
                .lambda()
                .ge(KqFormatDetail::getSignindate, LocalDate.now().minusDays(1))
                .le(KqFormatDetail::getSignindate, LocalDate.now())
                .eq(KqFormatDetail::getSignoutdate, ""));
        return one.getResourceid();
    }
}




