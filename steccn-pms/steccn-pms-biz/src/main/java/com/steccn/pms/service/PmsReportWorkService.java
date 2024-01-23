package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.pms.domain.PmsReportWork;
import com.steccn.pms.dto.*;
import com.steccn.pms.vo.*;
import com.steccn.qms.constant.QtManagerConstant;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_report_work(工单报工)】的数据库操作Service
 * @createDate 2022-09-28 15:18:51
 */
public interface PmsReportWorkService extends IService<PmsReportWork> {

    /**
     * 工序分页列表
     *
     * @param dto 条件
     * @return 分页数据
     */
    Page<ProcessReportWorkPageVO> pageList(ProcessReportWorkPageDTO dto);

    /**
     * 提交报工
     *
     * @param dto dto
     * @return 是否成功
     */
    Boolean submitReportWork(ProcessReportWorkSubmitDTO dto);

    /**
     * 报工记录
     *
     * @param dto 筛选条件
     * @return 分页报工记录
     */
    Page<ProcessReportWorkPageVO> recordPage(ProcessReportWorkRecordDTO dto);

    /**
     * 物料消耗
     *
     * @param dto dto
     * @return 物料消耗列表，不分页
     */
    List<ProcessReportWorkMaterialListDTO> materialConsume(ProcessReportWorkMaterialConsumeDTO dto);

    /**
     * 生产订单-工艺信息-报工信息
     *
     * @param dto
     * @return 分页报工信息
     */
    Page<PmsReportWorkVO> reportWorkPageList(PmsReportWorkPageDTO dto);

    /**
     * 根据班次分组获取指定日期的产量
     *
     * @param productLineId 产线id
     * @param date 指定日期
     * @return 产量map
     */
    Map<Long, PmsReportWork> dateProductNum(Integer productLineId, LocalDate date);

    /**
     * 根据开始结束时间查询数据
     *
     * @param startTime
     * @param endTime
     * @return
     */
    List<PmsReportWorkVO> listByClassTime(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取报工的班组信息
     * @param reportWorkIdSet 报工id集合
     * @return map
     */
    Map<Long, Long> reportWorkClassGroup(Set<Long> reportWorkIdSet);


    /**
     * 报工加工数预填
     * @param stationId 工位id
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    BigDecimal preload(Integer stationId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取物料消耗-线边仓数据
     * @param id
     * @return
     */
    List<ProcessReportWorkMaterialListDTO> materialLineWarehouse(Integer id);

    /**
     * 根据报工id获取
     * @param id 主键
     * @return
     */
    ProcessReportWorkSubmitDTO info(Long id);

    /**
     * 编辑工单报工信息
     * @param dto dto
     * @return 是否成功
     */
    Boolean edit(ProcessReportWorkSubmitDTO dto);

    /**
     * 报工删除
     * @param id 主键id
     * @return 是否成功
     */
    Boolean delete(Long id);

    /**
     * 深加工产线的产量和报工量
     * @param sjgDataList 深加工产线订单数据
     * @return list
     */
    List<EpMeterReadRowDTO> sjgOrderRw(List<EpMeterReadRowDTO> sjgDataList);

    /**
     * 获取指定日期，指定产线的所有产量
     * @param lineSet 产线集合
     * @param start 开始时间
     * @param end 结束时间
     * @return
     */
    Map<String, BigDecimal> classDateProductNum(Set<Integer> lineSet, LocalDate start, LocalDate end);

    /**
     * 车间-班次日期，班次对应的班组
     * @param field1
     * @param field2
     * @return
     */
    Map<String, Long> workshopClassGroupMap(Set<LocalDate> field1, Map<Integer, Set<Integer>> field2);

    /**
     * 根据产线以及时间获取报工记录
     */
    List<PmsReportWorkVO> listBylineIds(List<Integer> lineIds,LocalDate date);

    /**
     * 获取截至当前日期的报工数据
     * @param date
     * @param lineIds
     * @return
     */
    List<PmsReportWorkVO> monthlyData(LocalDate date, List<Integer> lineIds);

    /**
     * 根据报工记录获取物料的主物料（1个生产订单只有一个主物料）
     * @param reportIds
     * @return key 报工id value 物料id
     */
    Map<Long,Long> mainMaterialByReportWork(Set<Long> reportIds);

    ReportWorkPreinstallValueVO preinstallValue(LocalDate localDate);

    /**
     * 原料耗用量
     *
     * @param lineList 产线
     * @param start    开始时间
     * @param end      结束时间
     * @return
     */
    Map<String, BigDecimal> getUseCount(List<Integer> lineList, LocalDateTime start, LocalDateTime end);


    /**
     * 原料耗用量
     *
     * @param lineList 产线
     * @param start    开始时间
     * @param end      结束时间
     * @param unit     单位
     * @return
     */
    Map<String, BigDecimal> getUseCountUnit(List<Integer> lineList, LocalDateTime start, LocalDateTime end , String unit);


    /**
     * 原料耗用量-每个订单
     *
     * @param lineList 产线
     * @param start    开始时间
     * @param end      结束时间
     * @return
     */
    Map<String, BigDecimal> useCountPerOrder(List<Integer> lineList, LocalDateTime start, LocalDateTime end);

    /**
     * 原料耗用量-周|产线
     *
     * @param lineList 产线
     * @param start    开始时间
     * @param end      结束时间
     * @return
     */
    Map<String, BigDecimal> useCountWeek(List<Integer> lineList, LocalDateTime start, LocalDateTime end);

    /**
     * 原料耗用量-周|产线
     *
     * @param lineList 产线
     * @param start    开始时间
     * @param end      结束时间
     * @return
     */
    Map<String, BigDecimal> useCountMonth(List<Integer> lineList, LocalDateTime start, LocalDateTime end);

    /**
     * 获取产线的当天产量
     * @param lineList 产线集合
     * @param today 班次时间
     * @param type 类型
     * @return
     */
    Map<Long, Map<Integer, BigDecimal>> getTodayPm(List<Integer> lineList, LocalDate today, Integer type);

    /**
     * 获取产线的当天产量
     *
     * @param lineList 产线集合
     * @param start    开始日期
     * @param end      结束日期
     * @param type     类型
     * @return
     */
    Map<Long, BigDecimal> getDateRangePm(List<Integer> lineList, LocalDate start, LocalDate end, Integer type);

    /**
     * 获取日期范围内的产线产量
     *
     * @param lineList 产线集合
     * @param start    开始-班次时间
     * @param end      结束-班次时间
     * @param type     类型
     * @param unit     单位类型
     * @return
     */
    Map<Integer, BigDecimal> getDateRangePmUnit(List<Integer> lineList, LocalDate start, LocalDate end, int type, String unit);

    /**
     * 获取产线的当天入库数量
     *
     * @param lineIdList 产线集合
     * @param date       班次时间
     * @return
     */
    Map<Long, Map<Integer, BigDecimal>> getTodayInStorage(List<Integer> lineIdList, LocalDate date);

    /**
     * 获取日期范围内的入库数量
     *
     * @param lineIdList 产线集合
     * @param start      班次时间-开始
     * @param end        班次时间-结束
     * @return
     */
    Map<Long,BigDecimal> getDateRangeInStorage(List<Integer> lineIdList, LocalDate start, LocalDate end);


    /**
     * 获取日期范围内的入库数量
     *
     * @param lineIdList 产线集合
     * @param start      班次时间-开始
     * @param end        班次时间-结束
     * @param unit       单位
     * @return
     */
    Map<Integer, BigDecimal> getDateRangeInStorageUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, String unit);

    /**
     * 获取日期范围内的耗用量
     *
     * @param lineIdList 产线集合
     * @param start      班次时间-开始
     * @param end        班次时间-结束
     * @param unit       单位
     * @return
     */
    Map<Integer, BigDecimal> getDateRangeUseCountUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, String unit);

    /**
     * 获取生产订单的产量
     *
     * @param orderIdSet 生产订单集合
     * @param lineList 产线集合
     * @param type       类型 0基片 1深加工
     * @return
     */
    Map<Long, BigDecimal> getPmByOrder(List<Integer> lineList, Set<Long> orderIdSet, Integer type);

    /**
     * 获取生产订单的入库数量
     *
     * @param orderIdSet 生产订单集合
     * @return
     */
    Map<Long, BigDecimal> getInStorageByOrder(Set<Long> orderIdSet);

    /**
     * 获取产线班次产量  单位为unit
     * @param lineIdSet 产线
     * @param start 班次日期-开始
     * @param end 班次日期-结束
     * @param type 0基片 1深加工
     * @param unit 单位
     * @return
     */
    Map<String, BigDecimal> lineClassPm(Set<Integer> lineIdSet, LocalDate start, LocalDate end, int type, String unit);

    /**
     * 获取产线班次入库数量 单位为unit
     *
     * @param lineIdSet 产线
     * @param start     班次日期-开始
     * @param end       班次日期-结束
     * @param unit      单位
     * @return
     */
    Map<String, BigDecimal> lineClassInStorage(Set<Integer> lineIdSet, LocalDate start, LocalDate end, String unit);

    /**
     * 获取产线班次耗用数量 单位为unit
     *
     * @param lineIdSet 产线
     * @param start     班次日期-开始
     * @param end       班次日期-结束
     * @param unit      单位
     * @return
     */
    Map<String, BigDecimal> lineClassUseCount(Set<Integer> lineIdSet, LocalDate start, LocalDate end, String unit);


    /**
     * 获取产线耗用数量 单位为unit
     *
     * @param lineIdSet 产线
     * @param stationIds 工位
     * @param start     班次日期-开始
     * @param end       班次日期-结束
     * @return
     */
    Map<String, BigDecimal> lineUseCount(Set<Integer> lineIdSet, Set<Integer> stationIds, LocalDate start, LocalDate end, QtManagerConstant.QtGranularityEnum granularity);

    /**
     * 获取班次日期产量 单位为unit
     *
     * @param lineIdList 产线
     * @param start     班次日期-开始
     * @param end       班次日期-结束
     * @param type      类型 0基片 1深加工
     * @param unit      单位
     * @return
     */
    Map<String, BigDecimal> datePmUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, int type, String unit);

    /**
     * 获取班次日期产量 单位为unit
     *
     * @param lineIdList 产线
     * @param start     班次日期-开始
     * @param end       班次日期-结束
     * @param type      类型 0基片 1深加工
     * @param unit      单位
     * @return
     */
    Map<String, BigDecimal> dateClassPmUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, int type, String unit);


    /**
     * 获取班次日期入库 单位为unit
     *
     * @param lineIdList 产线
     * @param start     班次日期-开始
     * @param end       班次日期-结束
     * @param unit      单位
     * @return
     */
    Map<String, BigDecimal> dateInStorageUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, String unit);

    /**
     * 获取班次日期入库 单位为unit
     *
     * @param lineIdList 产线
     * @param start     班次日期-开始
     * @param end       班次日期-结束
     * @param unit      单位
     * @return
     */
    Map<String, BigDecimal> dateClassInStorageUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, String unit);

    /**
     * 获取停工时长
     * @param dto
     * @return
     */
    List<ProcessReportWorkHourVO> getReportWorkHour(ProcessReportWorkHourQmsDTO dto);

    /**
     * 获取班次日期，班次，产线的产量 key => line_date_classId
     * @param lineIdList 产线
     * @param start      班次日期-开始
     * @param end        班次日期-结束
     * @param type       0基片 1深加工
     * @param unit       单位
     * @return
     */
    Map<String, BigDecimal> dateClassLinePmUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, int type, String unit);

    /**
     * 获取班次日期，班次，产线的产量 key => line_date
     * @param lineIdList 产线
     * @param start      班次日期-开始
     * @param end        班次日期-结束
     * @param type       0基片 1深加工
     * @param unit       单位
     * @return
     */
    Map<String, BigDecimal> dateLinePmUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, int type, String unit);

    /**
     * 获取班次日期，班次，产线的产量 key => line_date_thick
     * @param lineIdList 产线
     * @param start      班次日期-开始
     * @param end        班次日期-结束
     * @param type       0基片 1深加工
     * @param unit       单位
     * @return
     */
    Map<String, BigDecimal> dateLineClassThickPmUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, int type, String unit);

    /**
     * 获取班次日期，班次，产线的入库量 key => line_date_classId
     *
     * @param lineIdList 产线
     * @param start      班次日期-开始
     * @param end        班次日期-结束
     * @param unit       单位
     * @return
     */
    Map<String, BigDecimal> dateClassLineInStorageUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, String unit);

    /**
     * 获取班次日期，班次，产线的入库量 key => line_date
     *
     * @param lineIdList 产线
     * @param start      班次日期-开始
     * @param end        班次日期-结束
     * @param unit       单位
     * @return
     */
    Map<String, BigDecimal> dateLineInStorageUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, String unit);


    /**
     * 获取生产批次关联的报工信息
     * @param batchSet 生产批次no集合
     * @return
     */
    Map<String, List<PmsReportWork>> batchLinkReportWorkMap(Set<String> batchSet);

    /**
     * 根据时间和产线获取产量map key - processid_lineid value - (pm(加工数-不良数),pm(加工数))
     *
     * @param lineIdList 产线集合
     * @param start      开始日期
     * @param end        结束日期
     * @return
     */
    Map<String, TupleDTO<BigDecimal, BigDecimal>> lineProcessPmMap(List<Integer> lineIdList, LocalDate start, LocalDate end);

    /**
     * 获取产线班次入库数量 单位为unit
     *
     * @param lineIdList 产线
     * @param start     班次日期-开始
     * @param end       班次日期-结束
     * @param unit      单位
     * @return
     */
    Map<Integer, BigDecimal> lineInStorage(List<Integer> lineIdList, LocalDate start, LocalDate end, String unit);

    /**
     * 获取钢化工位的报工产量
     *
     * @param start      开始日期
     * @param end        结束日期
     * @param lineIdList 产线集合
     * @return
     */
    Map<Integer, BigDecimal> getGhQuantity(LocalDate start, LocalDate end, List<Integer> lineIdList);

    /**
     * 基片统计报表 - 生产产量
     *
     * @param workShopId     车间主键
     * @param start          开始日期
     * @param end            结束日期
     * @param granularityInt 粒度
     * @param unit           单位
     * @return
     */
    CommonReportDTO jpPmDateGranularity(Integer workShopId, LocalDate start, LocalDate end, Integer granularityInt,String unit);

    /**
     * 基片统计报表 - 入库成品率
     *
     * @param workShopId     车间主键
     * @param start          开始日期
     * @param end            结束日期
     * @param granularityInt 粒度
     * @return
     */
    CommonReportDTO jpInStorageDateGranularity(Integer workShopId, LocalDate start, LocalDate end, Integer granularityInt);

    /**
     * 实际使用
     *
     * @param granularity 粒度
     * @param start       开始日期
     * @param end         结束日期
     * @param lineIdSet   产线id集合
     * @param bit         位数
     * @return
     */
    Map<String, BigDecimal> actualUse(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> lineIdSet, Integer bit);

    /**
     * 基片完工信息
     *
     * @param granularity 粒度
     * @param start       开始日期
     * @param end         结束日期
     * @param lineIdSet   产线集合
     * @param bit         位数
     * @return
     */
    Map<String, BigDecimal> jpComplete(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> lineIdSet, String unit , int bit);

    /**
     * 深加工完工信息
     *
     * @param granularity 粒度
     * @param start       开始日期
     * @param end         结束日期
     * @param lineIdSet   产线集合
     * @param bit         位数
     * @return
     */
    Map<String, BigDecimal> sjgComplete(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> lineIdSet, int bit);


    /**
     * 不良数
     * @param lineIdList
     * @param edgingStationIds
     * @param start
     * @param end
     * @param granularity
     * @return
     */
    Map<String, BigDecimal> badnessQuantity(Set<Integer> lineIdList, Set<Integer> edgingStationIds, LocalDate start, LocalDate end, QtManagerConstant.QtGranularityEnum granularity);
}
