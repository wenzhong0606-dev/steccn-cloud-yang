package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yequan
 * @desc 工序报工-提交报工
 */
@Data
@ApiModel("工序报工-提交报工")
public class ProcessReportWorkSubmitDTO implements Serializable {

    @ApiModelProperty("生产订单id")
    @NotNull(message = "生产订单不能为空")
    private Long produceOrderId;

    @ApiModelProperty("工序id")
    @NotNull(message = "工序不能为空")
    private Long processId;

    @ApiModelProperty("工位id")
    @NotNull(message = "工位不能为空")
    private Integer stationId;

    @ApiModelProperty("加工数量")
    @NotNull(message = "加工数量不能为空")
    @DecimalMin(value="0",message="加工数量应大于0")
    @DecimalMax(value="99999999.999",message="加工数量大小应小于99999999.999")
    @Digits(integer = 8,fraction = 3,message = "加工数量整数位上限为8位，小数位上限位3位")
    private BigDecimal processQuantity;

    @ApiModelProperty("不良数量")
    @NotNull(message = "不良数量不能为空")
    @DecimalMin(value="0",message="不良数量应大于0")
    @DecimalMax(value="99999999.999",message="不良数量大小应小于99999999.999")
    @Digits(integer = 8,fraction = 3,message = "不良不良数量整数位上限为8位，小数位上限位3位")
    private BigDecimal badnessQuantity;

    @ApiModelProperty("单位id")
    @NotNull(message = "单位不能为空")
    private Long unitId;

    @ApiModelProperty("批次主键")
    @NotNull(message = "批次不能为空")
    private Long batch;

    @ApiModelProperty("批次no")
    private String batchNo;

    @ApiModelProperty("产线id")
    @NotNull(message = "产线不能为空")
    private Integer productLineId;

    @ApiModelProperty("班次id")
    @NotNull(message = "班次不能为空")
    private Long classesId;

    @ApiModelProperty("班组id")
    @NotNull(message = "班组不能为空")
    private Long classGroupId;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("加工时间")
    @Valid
    private List<ProcessReportWorkHourDTO> hourDTOList;

    @ApiModelProperty("物料消耗")
    @Valid
    private List<ProcessReportWorkMaterialListDTO> materialConsumeList;

    @ApiModelProperty("能耗")
    private List<ProcessReportWorkConsumeDTO> consumeDTOList;

    private static final long serialVersionUID = 1L;
}
