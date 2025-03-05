package com.steccn.oa.service;

import com.steccn.oa.domain.HpelementNotice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.oa.vo.OaHpElementNoticeVO;

import java.util.List;

/**
* @author Admin
* @description 针对表【hpelement_notice】的数据库操作Service
* @createDate 2025-02-20 14:19:24
*/
public interface HpelementNoticeService extends IService<HpelementNotice> {
    List<OaHpElementNoticeVO> queryNoticeNews();

    OaHpElementNoticeVO queryNoticeNewsDetails(Integer id);
}
