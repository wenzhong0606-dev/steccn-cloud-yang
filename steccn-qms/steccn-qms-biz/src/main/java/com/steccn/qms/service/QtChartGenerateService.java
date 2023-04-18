package com.steccn.qms.service;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.pms.vo.ProducePassRateVO;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtChartSubDetailDTO;
import com.steccn.qms.dto.QtTimeDTO;
import com.steccn.qms.vo.QtChartBarVO;
import com.steccn.common.vo.QtChartPieVO;
import com.steccn.qms.vo.QtDefectChartVO;

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
     * @param dto
     * @return
     */
    List<QtChartSubDetailDTO> generateSubstrateLossData(QtChartParamDTO dto);

    /**
     * 生成基片成品率
     * @param dto
     * @return
     */
    QtDefectChartVO generateSubstrateOverview(QtTimeDTO dto);

    /**
     * 生产基片成品率柱状趋势图
     * @param dto
     * @return
     */
    QtChartBarVO generateSubstrateBarTend(QtChartParamDTO dto);

    /**
     * 生成基片成品率饼状趋势图
     * @return
     */
    QtChartPieVO generateSubstratePieTend(QtChartParamDTO dto);

    /**
     * 对list进行过滤
     * @param rList
     * @param dto
     * @return
     */
    public List<ProducePassRateVO> rListFilter(List<ProducePassRateVO> rList, QtChartParamDTO dto);

    /**
     * 生成基片损耗柱状图
     * @param dto
     * @return
     */
    public QtLineChartBarDTO generateSubstrateBarLoss(QtChartParamDTO dto);

    /**
     * 生成基片损耗饼图
     * @param dto
     * @return
     */
    public QtChartPieVO generateSubstratePieLoss(QtChartParamDTO dto);
}
