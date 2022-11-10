package com.steccn.qms.service;

import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtChartSubDetailDTO;
import com.steccn.qms.vo.QtChartBarVO;

import java.util.List;

/**
 * 对图表进行计算
 *
 * @author AndrewLuo
 * @date 2022/11/10
 */
public interface QtChartGenerateService {
    /**
     * 生成基片损耗趋势图
     * @return
     */
    List<QtChartSubDetailDTO> generateSubstrateLoss(QtChartParamDTO dto);
}
