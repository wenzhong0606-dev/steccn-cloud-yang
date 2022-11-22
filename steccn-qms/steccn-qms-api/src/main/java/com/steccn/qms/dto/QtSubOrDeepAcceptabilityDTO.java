package com.steccn.qms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel("深加工或基片合格率DTO")
public class QtSubOrDeepAcceptabilityDTO extends BasePageDTO implements Serializable {

    @ApiModelProperty("深加工或者基片")
    private String name;

    @ApiModelProperty("开始时间")
    @NotNull(message = "开始时间不能为空")
    private LocalDate dateStart;

    @ApiModelProperty(value = "开始时间",hidden = true)
    private LocalDateTime dateTimeStart;

    @ApiModelProperty("结束时间")
    @NotNull(message = "结束时间不能为空")
    private LocalDate dateEnd;

    @ApiModelProperty(value = "结束时间",hidden = true)
    private LocalDateTime dateTimeEnd;

    @ApiModelProperty("对比")
    private Integer compare;

    @ApiModelProperty("生产车间")
    private Integer productWorkShopId;

    @ApiModelProperty("产线")
    private Integer productLineId;

    @ApiModelProperty("班次")
    private Integer workingOrder;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("客户")
    private Integer customer;

    @ApiModelProperty("类型:0柱状图,1饼状图")
    private Integer type;

    @ApiModelProperty(value = "任务类型 来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7", hidden = true)
    private Integer taskType;

    @ApiModelProperty("取样日期")
    private LocalDate samplingTime;

    @ApiModelProperty("取样开始时间")
    private LocalDateTime samplingStartTime;

    @ApiModelProperty("取样结束时间")
    private LocalDateTime samplingEndTime;

    @ApiModelProperty("物料使用车间id")
    private Integer materialWorkshopId;

    @ApiModelProperty("供应商id")
    private Long supplierId;

    @ApiModelProperty("对比内容")
    private List<String> compareContent;

    @ApiModelProperty(value = "图表的过滤条件")
    private List<String> filters;

    @ApiModelProperty(value = "图片base64")
    private String base64;

    @ApiModelProperty("巡检合格率l类型")
    private Integer inspectionType;// 1-深加工 0-基片

    @ApiModelProperty("对比名称")
    private String compareName;



    @ApiModelProperty(value = "明细图片base64")
    private String detailBase64;
}
