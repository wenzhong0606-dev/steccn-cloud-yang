package com.steccn.pms.service;

import com.steccn.pms.domain.PmsProducePlan;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsProducePlanDTO;

import java.util.List;

/**
* @author yequan
* @description 针对表【pms_produce_plan(生产月计划)】的数据库操作Service
* @createDate 2022-11-02 10:10:08
*/
public interface PmsProducePlanService extends IService<PmsProducePlan> {

    /**
     * 生产月计划列表
     *
     * @param monthDate
     * @return
     */
    List<PmsProducePlanDTO> list(String monthDate);

    /**
     * 批量新增
     *
     * @param dto
     * @return
     */
    Boolean addOrEditBatch(PmsProducePlanDTO dto);

}
