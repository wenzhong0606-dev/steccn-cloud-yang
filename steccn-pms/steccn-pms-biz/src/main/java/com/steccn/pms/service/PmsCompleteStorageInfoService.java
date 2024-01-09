package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsCompleteStorageDTO;
import com.steccn.pms.dto.PmsCompleteStorageNumDTO;
import com.steccn.pms.dto.PmsProducerOrderInfoDTO;
import com.steccn.pms.vo.FurtherProduceReportDataVO;
import com.steccn.pms.vo.PmsCompleteStorageVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_complete_storage_info(完工入库-详情)】的数据库操作Service
 * @createDate 2022-09-28 15:18:51
 */
public interface PmsCompleteStorageInfoService extends IService<PmsCompleteStorageInfo> {

    /**
     * 入库单已存在箱号
     *
     *
     * @param id
     * @param caseNoSet 箱号set
     * @return 是否
     */
    Boolean existCaseNo(Long id, Set<String> caseNoSet);


    /**
     * 根据订单id获取数据详情分页
     *
     * @param dto
     * @return
     */
    Page<PmsCompleteStorageVO> pageByOrderId(PmsCompleteStorageDTO dto);


    /**
     * 根据订单id获取完工数量单位
     *
     * @param orderSet
     * @return
     */
    Map<Long, PmsCompleteStorageNumDTO> numByOrderId(Set<Long> orderSet);

    /**
     * 根据完成订单入库单号获取箱号编码
     *
     * @param id
     * @return
     */
    List<String> getCaseNo(Long id);

    /**
     * 查询物料的入库
     *
     * @param materialSet 物料集合
     * @param date 指定日期
     * @return map key为物料id value 【key为班次id，value为数量】
     */
    Map<Long, Map<Long, BigDecimal>> materialMap(Set<Long> materialSet , LocalDate date);

    /**
     * 查询物料的入库
     *
     * @param materialSet 物料集合
     * @param date 指定日期
     * @return map key为物料id value 【key为班次id，value为数量】 key=> 产线id_物料id_班次id 入库数
     */
    Map<String , BigDecimal> materialMap(List<Integer> lineIdList, Set<Long> materialSet, LocalDate date);


    /**
     * 入库
     *
     * @param lineList 产线集合
     * @param start 开始时间
     * @param end 结束时间
     * @return map key为物料id value 【key为班次id，value为数量】
     */
    Map<String, BigDecimal> inStorage(List<Integer> lineList, LocalDateTime start, LocalDateTime end);

    /**
     * 查询物料的入库 （截至当前日期的当月数据）
     * @param materialSet 物料集合
     * @param date 指定日期
     * @return map key为物料id value 【key为班次id，value为数量】
     */
    Map<Long, Map<Long, BigDecimal>> materialByDateMap(Set<Long> materialSet , LocalDate date);


    /**
     * 查询物料的入库（截至当前日期的当月数据）
     * @param materialSet
     * @param date
     * @return
     */
    List<FurtherProduceReportDataVO> materialByDateList(Set<Long> materialSet , LocalDate date);

    /**
     * 入库数 - 周
     *
     * @param lineList 产线集合
     * @param start    开始时间
     * @param end      结束时间
     * @return map key为产线id+"_"+week value 【key为班次id，value为数量】
     */
    Map<String, BigDecimal> inStorageWeek(List<Integer> lineList, LocalDateTime start, LocalDateTime end);

    /**
     * 入库数 - 月
     *
     * @param lineList 产线集合
     * @param start    开始时间
     * @param end      结束时间
     * @return map key为产线id + "_" + month  value 【key为班次id，value为数量】
     */
    Map<String, BigDecimal> inStorageMonth(List<Integer> lineList, LocalDateTime start, LocalDateTime end);


    /**
     * 根据箱号获取生产批次号 生产订单号以及物料名称
     * @param caseNo
     * @return
     */
    PmsProducerOrderInfoDTO getNumberByCaseNo(String caseNo);


    /**
     * 根据该车间对应时间的完工入库记录进行汇总
     * @param dto
     * @return
     */
    List<CommonReportDTO> substrate(DpPmsOperateDTO dto);

    /**
     * 基片统计报表-产线产量明细
     * @param dto
     * @return
     */
    List<CommonReportDTO> substrateProduceLine(DpPmsOperateDTO dto);
}
