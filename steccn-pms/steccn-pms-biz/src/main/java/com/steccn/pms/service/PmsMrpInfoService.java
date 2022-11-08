package com.steccn.pms.service;

import com.steccn.pms.domain.PmsMrpInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.domain.PmsProducePlan;
import com.steccn.pms.dto.PmsProducePlanDTO;

import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_mrp_info(物料需求计划详情)】的数据库操作Service
 * @createDate 2022-11-02 10:10:08
 */
public interface PmsMrpInfoService extends IService<PmsMrpInfo> {

    /**
     * 增加物料需求计划详情
     *
     * @param dto
     * @return
     */
    Boolean add(PmsProducePlanDTO dto);

    /**
     * 下达进行mrp运算
     *
     * @param list
     * @return
     */
    Boolean mrpAdd(Set<Long> list);
}
