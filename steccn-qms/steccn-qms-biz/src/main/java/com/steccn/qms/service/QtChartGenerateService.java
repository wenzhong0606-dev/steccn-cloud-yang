package com.steccn.qms.service;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.ValidList;
import com.steccn.pms.dto.PmsPullForceEditDTO;
import com.steccn.pms.vo.ProducePassRateVO;
import com.steccn.qms.dto.*;
import com.steccn.qms.vo.QtChartBarVO;
import com.steccn.common.vo.QtChartPieVO;
import com.steccn.qms.vo.QtDefectChartVO;

import java.time.LocalDate;
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
    List<ProducePassRateVO> rListFilter(List<ProducePassRateVO> rList, QtChartParamDTO dto);

    /**
     * 生成基片损耗柱状图
     * @param dto
     * @return
     */
    QtLineChartBarDTO generateSubstrateBarLoss(QtChartParamDTO dto);

    /**
     * 生成基片损耗饼图
     * @param dto
     * @return
     */
    QtChartPieVO generateSubstratePieLoss(QtChartParamDTO dto);

    /**
     * 计算拉引量
     * @param classTimeStart
     * @param classTimeEnd
     * @return
     */
    List<QtPullAmountDTO> calculatePullAmount(LocalDate classTimeStart,LocalDate classTimeEnd);

    /**
     * 计算拉引量
     * @return
     */
    Boolean calculatePullAmountTask();

    /**
     * 计算ems拉引量
     * @param dto
     * @return
     */
    List<QtEmsPullAmountDTO> calculateEmsPullAmount(ValidList<QtEmsPullAmountDTO> dto);
}
