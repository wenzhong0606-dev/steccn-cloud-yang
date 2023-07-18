package com.steccn.qms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QtDefectProductDTO {

    private Long id;

    /**
     * 不合格品处理单号
     */
    private String code;

    /**
     * 标题
     */
    private String title;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 检验任务主键
     */
    private Long taskId;

    /**
     * 检验任务单号
     */
    private String taskCode;

    /**
     * 检验任务类型
     */
    private Integer taskType;

    /**
     * 检验任务类型名称
     */
    private String taskTypeName;

    /**
     * 检验完成人员
     */
    private Long taskFinishMan;


    /**
     * 检验完成人员名称
     */
    private String taskFinishManName;

    /**
     * 检验完成时间
     */
    private LocalDateTime taskFinishTime;

    /**
     * 物料主键
     */
    private Long materialId;

    /**
     * 物料编码
     */
    private String materialCode;


    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 生产订单号
     */
    private String produceOrderNo;

    /**
     * 生产批次号
     */
    private String produceBatchNo;

    /**
     * 供应商
     */
    private Integer supplier;

    /**
     * 样本数量
     */
    private BigDecimal sampleQuantity;

    /**
     * 合格数量
     */
    private BigDecimal goodQuantity;

    /**
     * 不合格数量
     */
    private BigDecimal badQuantity;

    /**
     * 处理人
     */
    private Long dealMan;


    /**
     * 处理人名称
     */
    private String dealManName;

    /**
     * 0-待处理、1-待审批、2-已关闭
     */
    private Integer status;

    /**
     * 处理方式
     */
    private Integer dealWay;


    /**
     * 处理方式名称
     */
    private String dealWayName;

    /**
     * 处理数量
     */
    private BigDecimal dealQuantity;

    /**
     * 处理时间
     */
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class},message = "处理时间不能为空")
    private LocalDateTime dealTime;

    /**
     * 备注
     */
    private String note;

    /**
     * 抄送人
     */
    private List<QtCarbonCopyManDTO> manList;

    /**
     * 审批人
     */
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class},message = "审批人不能为空")
    private Long approveMan;


    /**
     * 不合格品详情页url
     */
    private String infoUrl;
}
