package com.steccn.sales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.vo.EpDeepConsumeContrastVO;
import com.steccn.sales.domain.KsSendNotice;
import io.swagger.models.auth.In;

import java.time.LocalDate;

/**
* @author Admin
* @description 针对表【ks_send_notice(发货通知单)】的数据库操作Service
* @createDate 2023-11-29 15:13:10
*/
public interface KsSendNoticeService extends IService<KsSendNotice> {
    /**
     * 客户下单量
     * @param start
     * @param end
     * @return
     */
    EpDeepConsumeContrastVO orderQuantityByCus(LocalDate start, LocalDate end, Integer type);

    /**
     * 下单产品规格
     * @param start
     * @param end
     * @return
     */
    EpDeepConsumeContrastVO orderQuantityBySpe(LocalDate start, LocalDate end, Integer type);

    /**
     * 下单产品属性
     * @param start
     * @param end
     * @return
     */
    EpDeepConsumeContrastVO orderQuantityByAttr(LocalDate start, LocalDate end, Integer type);

    /**
     * 下单产品分类
     * @param start
     * @param end
     * @return
     */
    EpDeepConsumeContrastVO orderQuantityByCls(LocalDate start, LocalDate end, Integer type);

    /**
     * 下单产品厚度
     * @param start
     * @param end
     * @return
     */
    EpDeepConsumeContrastVO orderQuantityByThick(LocalDate start, LocalDate end, Integer type);
}
