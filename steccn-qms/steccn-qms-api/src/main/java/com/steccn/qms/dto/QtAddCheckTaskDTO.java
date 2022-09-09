package com.steccn.qms.dto;

import com.steccn.common.valid.InsertGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 新增巡检任务DTO
 *
 * @author AndrewLuo
 * @date 2022/09/09
 */
@Data
public class QtAddCheckTaskDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer type;

    /**
     * 物料id
     */
    @NotNull(groups = {InsertGroup.class}, message = "物料id不能为空")
    private Long materialId;

    /**
     * 车间
     */
    @NotNull(groups = {InsertGroup.class}, message = "车间id不能为空")
    private Integer workshopId;

    /**
     * 产线
     */
    @NotNull(groups = {InsertGroup.class}, message = "产线id不能为空")
    private Integer productLineId;

    /**
     * 生产批次
     */
    private String productBatch;

    /**
     * 生产工单
     */
    private String productOrder;

    /**
     * 检验方案id
     */
    @NotNull(groups = {InsertGroup.class}, message = "检验方案不能为空")
    private Integer checkPlanId;
}
