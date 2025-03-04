package com.steccn.oa.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.oa.domain.HpelementImgMatch;
import com.steccn.oa.domain.HpelementNotice;
import com.steccn.oa.service.HpelementImgMatchService;
import com.steccn.oa.service.HpelementNoticeService;
import com.steccn.oa.mapper.HpelementNoticeMapper;
import com.steccn.oa.vo.OaHpElementNoticeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【hpelement_notice】的数据库操作Service实现
* @createDate 2025-02-20 14:19:24
*/
@Service
@RequiredArgsConstructor
public class HpelementNoticeServiceImpl extends ServiceImpl<HpelementNoticeMapper, HpelementNotice>
    implements HpelementNoticeService{
    private final HpelementImgMatchService hpelementImgMatchService;

    @Override
    public List<OaHpElementNoticeVO> queryNoticeNews() {
        List<HpelementNotice> datas = list(new LambdaQueryWrapper<HpelementNotice>()
                .orderByDesc(HpelementNotice::getCreatedate, HpelementNotice::getCreatetime)
                .last("LIMIT 20"));
        List<OaHpElementNoticeVO> oaHpElementNoticeVOS = BeanUtil.copyToList(datas, OaHpElementNoticeVO.class);
        List<HpelementImgMatch> list = hpelementImgMatchService.list(new LambdaQueryWrapper<HpelementImgMatch>()
                .in(HpelementImgMatch::getNoticeId, datas.stream().map(HpelementNotice::getId).collect(Collectors.toSet())));
        Map<Integer, String> urlMap = list.stream().collect(Collectors.toMap(p -> p.getNoticeId(), p -> p.getImgUrl()));
        oaHpElementNoticeVOS.forEach(tmp->{
            tmp.setImgsrc(urlMap.get(tmp.getId()));
        });
        return oaHpElementNoticeVOS;
    }
}




