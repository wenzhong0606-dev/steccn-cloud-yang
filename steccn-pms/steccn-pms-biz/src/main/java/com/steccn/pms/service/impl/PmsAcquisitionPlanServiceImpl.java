package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.dto.PmsAcquisitionPlanPageDTO;
import com.steccn.pms.service.PmsAcquisitionPlanService;
import com.steccn.pms.vo.PmsAcquisitionPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/11/4
 */
@Service
@RequiredArgsConstructor
public class PmsAcquisitionPlanServiceImpl implements PmsAcquisitionPlanService {
    @Override
    public Page<PmsAcquisitionPlanVO> pageList(PmsAcquisitionPlanPageDTO dto) {
        return null;
    }
}
