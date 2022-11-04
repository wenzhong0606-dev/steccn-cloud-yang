package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.dto.PmsAcquisitionPlanPageDTO;
import com.steccn.pms.vo.PmsAcquisitionPlanVO;

/**
 * 领料计划Service
 *
 * @author Mongknife
 * @date 2022/11/4
 */
public interface PmsAcquisitionPlanService{
    /**
     * 领料计划分页列表
     * @param dto
     * @return
     */
    Page<PmsAcquisitionPlanVO> pageList(PmsAcquisitionPlanPageDTO dto);
}
