package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.constant.ResultCode;
import com.steccn.pms.domain.PmsReportWork;
import com.steccn.pms.dto.*;
import com.steccn.pms.vo.PmsReportWorkVO;
import com.steccn.pms.vo.ProcessReportWorkInfoVO;
import com.steccn.pms.vo.ProcessReportWorkPageVO;

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
}
