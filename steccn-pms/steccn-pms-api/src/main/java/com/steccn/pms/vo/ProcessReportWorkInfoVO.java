package com.steccn.pms.vo;

import com.steccn.pms.dto.ProcessReportWorkHourDTO;
import com.steccn.pms.dto.ProcessReportWorkMaterialListDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yequan
 * @desc 报工详情
 * @date 2023/3/22
 */
@Data
@ApiModel("报工详情")
public class ProcessReportWorkInfoVO implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("报工信息编码")
    private String code;

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("加工数量")
    private BigDecimal processQuantity;

    @ApiModelProperty("不良数量")
    private BigDecimal badnessQuantity;

    @ApiModelProperty("数量单位")
    private Long unitId;

    @ApiModelProperty("批次")
    private String batchNo;

    @ApiModelProperty("班次id")
    private Long classesId;

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("工位id")
    private Integer stationId;

    @ApiModelProperty("班组id")
    private Long classGroupId;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("生产订单编码")
    private String produceOrderNo;

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;

    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("加工时间")
    private List<ProcessReportWorkHourDTO> hourDTOList;

    @ApiModelProperty("物料消耗")
    private List<ProcessReportWorkMaterialListDTO> materialConsumeList;

    private static final long serialVersionUID = 1L;
}
