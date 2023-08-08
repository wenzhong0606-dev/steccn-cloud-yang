package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.domain.PmsWorkOrderBatch;
import com.steccn.pms.dto.PmsDefectDayOrderBasicDTO;
import com.steccn.pms.dto.ProduceOrderBatchDto;
import com.steccn.pms.vo.WorkOrderBatchVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_work_order_batch(生产工单-批次信息)】的数据库操作Service
 * @createDate 2022-09-28 15:18:51
 */
public interface PmsWorkOrderBatchService extends IService<PmsWorkOrderBatch> {


    /**
     * 根据主键id查询批次的信息
     *
     * @param idList 批次id集合
     * @return
     */
    Map<Long, WorkOrderBatchVO> listById(Set<Long> idList);

    /**
     * 根据订单id查询该生产订单的批次信息
     *
     * @param idList
     * @return
     */
    Map<Long, List<PmsWorkOrderBatch>> listByOrderId(Set<Long> idList);

    /**
     * 根据订单id查询正在执行中批次信息
     *
     * @param orderId
     * @return
     */
    PmsWorkOrderBatch getByOrderId(Long orderId);

    /**
     * 根据订单id查询正在执行中批次信息
     *
     * @param orderId
     * @return
     */
    List<PmsWorkOrderBatch> getAllBatchByOrderId(Long orderId);


    /**
     * 批次新增
     *
     * @param dto
     * @return
     */
    Boolean add(ProduceOrderBatchDto dto);

    /**
     * 根据批次编码获取批次信息
     *
     * @param batchNo 批次编码
     * @return 批次信息
     */
    PmsWorkOrderBatch getByCode(String batchNo);

    /**
     * 查询不良日报表
     * @param lineList 产线
     * @param start 开始日期
     * @param end 结束日期
     * @return list
     */
    List<PmsDefectDayOrderBasicDTO> defectDay(List<Integer> lineList, LocalDateTime start, LocalDateTime end);
}
