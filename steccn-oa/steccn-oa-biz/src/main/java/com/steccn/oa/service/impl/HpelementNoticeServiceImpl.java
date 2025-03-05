package com.steccn.oa.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.domain.HpelementImgMatch;
import com.steccn.oa.domain.HpelementNotice;
import com.steccn.oa.service.HpelementImgMatchService;
import com.steccn.oa.service.HpelementNoticeService;
import com.steccn.oa.mapper.HpelementNoticeMapper;
import com.steccn.oa.vo.OaHpElementNoticeVO;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
                .in(HpelementImgMatch::getNoticeId, datas.stream().map(HpelementNotice::getId).collect(Collectors.toSet()))
                .eq(HpelementImgMatch::getIsMain,OaConstant.YES));
        Map<Integer, String> urlMap = list.stream().collect(Collectors.toMap(p -> p.getNoticeId(), p -> p.getImgUrl()));
        oaHpElementNoticeVOS.forEach(tmp->{
            tmp.setImgsrc(urlMap.get(tmp.getId()));
        });
        return oaHpElementNoticeVOS;
    }

    @Override
    public OaHpElementNoticeVO queryNoticeNewsDetails(Integer id) {
        HpelementNotice byId = getById(id);
        List<HpelementImgMatch> list = hpelementImgMatchService.list(new LambdaQueryWrapper<HpelementImgMatch>()
                .eq(HpelementImgMatch::getNoticeId, id)
                .eq(HpelementImgMatch::getIsMain, OaConstant.NO));
        Map<String, String> urlMap = list.stream().collect(Collectors.toMap(p -> p.getImgSrc(), p -> p.getImgUrl()));
        Document document = Jsoup.parse(byId.getContent());

        // 获取所有图片
        Elements imgElements = document.select("img");

        // 遍历替换图片路径
        for (Element img : imgElements) {
            String oldSrc = img.attr("src");

            // 替换逻辑（这里演示替换成固定的URL，你可以按需替换）
            String newSrc = urlMap.get(oldSrc);
            img.attr("src", newSrc);
        }

        // 输出替换后的HTML
        String modifiedHtml = document.html();
        byId.setContent(modifiedHtml);
        OaHpElementNoticeVO vo = new OaHpElementNoticeVO();
        BeanUtil.copyProperties(byId,vo);
        return vo;
    }
}




