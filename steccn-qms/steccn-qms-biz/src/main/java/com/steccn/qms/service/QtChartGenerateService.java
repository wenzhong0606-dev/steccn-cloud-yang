package com.steccn.qms.service;

import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.vo.QtChartBarVO;

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
    QtChartBarVO generateSubstrateLoss(QtChartParamDTO dto);
}
