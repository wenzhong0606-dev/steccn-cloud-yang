package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.TupleDTO;
import com.steccn.pms.domain.PmsMonthlyProductionPlanNew;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【pms_monthly_production_plan_new】的数据库操作Service
 * @createDate 2024-11-11 13:54:35
 */
public interface PmsMonthlyProductionPlanNewService extends IService<PmsMonthlyProductionPlanNew> {

    /**
     * 插入物料，客户，月度数据
     *
     * @param date       日期
     * @param clientId   客户
     * @param materialId 物料主键
     * @return
     */
    Boolean insertData(LocalDate date, Integer clientId, Long materialId);

    /**
     * 删除
     *
     * @param date       日期
     * @param clientId   客户
     * @param materialId 物料主键
     * @return
     */
    Boolean deleteData(LocalDate date, Integer clientId, Long materialId);

    /**
     * 下达
     *
     * @param linkId     关联主键
     * @param date       日期
     * @param clientId   客户
     * @param materialId 物料主键
     * @return
     */
    Boolean release(Long linkId, String date, Integer clientId, Long materialId);

    /**
     * 编辑
     *
     * @param date       日期
     * @param clientId   客户
     * @param materialId 物料主键
     * @param value      日期数据
     * @return
     */
    Boolean updateData(LocalDate date, Integer clientId, Long materialId, List<TupleDTO<LocalDate, BigDecimal>> value);
}
