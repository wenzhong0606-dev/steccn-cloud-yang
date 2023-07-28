package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsCompleteStorageDTO;
import com.steccn.pms.dto.PmsCompleteStorageNumDTO;
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

}
