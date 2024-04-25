package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.pms.domain.PmsPullForce;
import com.steccn.pms.dto.PmsPullForceDashboardDTO;
import com.steccn.pms.dto.PmsPullForceEditDTO;
import com.steccn.pms.dto.PmsPullForceQueryDTO;
import com.steccn.pms.dto.PullForceSumDTO;
import com.steccn.pms.vo.PmsPullForceDataVO;
import com.steccn.pms.vo.PmsPullForceDetailsVO;
import com.steccn.pms.vo.PmsPullForceVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_pull_force(拉引力表)】的数据库操作Service
 * @createDate 2022-11-02 10:10:08
 */
public interface PmsPullForceService extends IService<PmsPullForce> {

    /**
     * 拉引量列表
     *
     * @param workshopId
     * @return
     */
    List<PmsPullForceVO> list(Integer workshopId, String monthDate);

    /**
     * 拉引量详情
     *
     * @param workshopId
     * @return
     */
    List<PmsPullForceDetailsVO> details(Integer workshopId, String pullForceDate);

    /**
     * 拉引量批量编辑
     *
     * @param dtoList
     * @return
     */
    Boolean editBatch(List<PmsPullForceEditDTO> dtoList);


    /**
     * 获取一个产线2个班次的拉引量
     *
     * @param productLineId 产线id
     * @param date          日期
     * @param classGroupIdList 班次id集合
     * @return list
     */
    Map<Long, PmsPullForce> pullForceDay(Integer productLineId, LocalDate date, List<Long> classGroupIdList);


    /**
     * 获取多个车间下的拉引量
     * @param date 日期
     * @param productWorkshopIds 车间id集合
     * @return 一个产线下的拉引量信息
     */
    Map<Integer, List<PmsPullForce>> pullForceDayByWorkshop(LocalDate date,List<Integer> productWorkshopIds);

    /**
     * 根据班次日期+产线+班次查询拉引量
     * @param pfSet 条件集合
     * @return
     */
    Map<String, BigDecimal> getPullForce(Set<String> pfSet);

    /**
     * 拉引量值预填 如果存在则覆盖
     * @param list
     * @return
     */
    Boolean edit(List<PmsPullForceEditDTO> list);


    /**
     * 获取截至日期的当月拉引量数据
     * @param date
     * @param productWorkshop
     * @return
     */
    List<PmsPullForce> monthlyData(LocalDate date, List<Integer> productWorkshop);

    /**
     * 时间范围内的拉引量数据
     * @param dto
     * @return
     */
    List<PmsPullForceDataVO> getDataFromPeriods(PmsPullForceQueryDTO dto);

    /**
     * 查询班次日期的拉引量 key = classDate +""
     *
     * @param lineIdList 产线
     * @param start      开始日期
     * @param end        结束日期
     * @return
     */
    Map<String, BigDecimal> datePullForce(List<Integer> lineIdList, LocalDate start, LocalDate end);

    /**
     * 查询班次日期的拉引量 key = workshopId  value 车间拉引量
     *
     * @param workshopIdList 产线
     * @param date           班次日期
     * @return
     */
    Map<Integer, BigDecimal> dateWsPullForce(List<Integer> workshopIdList, LocalDate date);

    /**
     * 查询班次日期的拉引量 key = classDate +"_"+classId
     *
     * @param lineIdList 产线
     * @param start      开始日期
     * @param end        结束日期
     * @return
     */
    Map<String, BigDecimal> dateClassPullForce(List<Integer> lineIdList, LocalDate start, LocalDate end);

    /**
     * 查询产线+班次日期+班次的拉引量
     *
     * @param lineIdList 产线
     * @param start      开始日期
     * @param end        结束日期
     * @return
     */
    Map<String, BigDecimal> dateClassLinePmUnit(List<Integer> lineIdList, LocalDate start, LocalDate end);

    /**
     * 查询产线+班次日期的拉引量
     *
     * @param lineIdList 产线
     * @param start      开始日期
     * @param end        结束日期
     * @return
     */
    Map<String, BigDecimal> dateLinePmUnit(List<Integer> lineIdList, LocalDate start, LocalDate end);

    /**
     * 基片统计报表 - 获取车间的拉引量
     *
     * @param workShopId     车间主键
     * @param start          开始日期
     * @param end            结束日期
     * @param granularityInt 粒度整数
     * @return
     */
    CommonReportDTO jpPfDateGranularity(Integer workShopId, LocalDate start, LocalDate end, Integer granularityInt);

    /**
     * 拉引量汇总
     * @param dto
     * @return
     */
    Map<String, BigDecimal> pullForceSum(PullForceSumDTO dto);

    /**
     * 拉引量报表
     * @param dto
     * @return
     */
    SteccnTableDataDTO<Map<String,String>> pullForceReport(PmsPullForceDashboardDTO dto);
}
