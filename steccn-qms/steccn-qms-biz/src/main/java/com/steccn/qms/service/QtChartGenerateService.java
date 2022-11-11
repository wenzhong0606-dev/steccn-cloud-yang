package com.steccn.qms.service;

import com.steccn.qms.dto.QtChartDateRangeDTO;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtChartSubDetailDTO;
import com.steccn.qms.vo.QtChartBarVO;
import com.steccn.qms.vo.QtDefectChartVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对图表进行计算
 *
 * @author AndrewLuo
 * @date 2022/11/10
 */
public interface QtChartGenerateService {
    /**
     * 生成基片损耗全量数据表
     * @return
     */
    List<QtChartSubDetailDTO> generateSubstrateLossData(QtChartParamDTO dto);

    /**
     * 生成基片成品率图
     */
    QtDefectChartVO generateSubstrateOverview(QtChartDateRangeDTO dto);
}
