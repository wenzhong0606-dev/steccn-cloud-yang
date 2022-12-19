package com.steccn.wms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 物流单分页DTO
 *
 * @author Mongknife
 * @date 2022/11/30
 */
@Data
@ApiModel("物流单分页DTO")
public class WmsLogisticsOrderPageDTO extends BasePageDTO {

    @ApiModelProperty("物流单号")
    private String code;

    @ApiModelProperty("类型")
    private Long type;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("物流方")
    private Integer logisticsCompany;

    @ApiModelProperty("创建开始日期")
    private LocalDate createStartDate;

    @ApiModelProperty("创建结束时间")
    private LocalDate createEndDate;

    @ApiModelProperty("创建人")
    private String createName;

    @ApiModelProperty("预计送达开始日期")
    private LocalDate predictStartDate;

    @ApiModelProperty("预计送达结束日期")
    private LocalDate predictEndDate;

    @ApiModelProperty("实际送达开始日期")
    private LocalDate practicalStartDate;

    @ApiModelProperty("实际送达结束日期")
    private LocalDate practicalEndDate;

    @ApiModelProperty("预计送达开始时间")
    private LocalDateTime predictStartTime;

    @ApiModelProperty("预计送达结束时间")
    private LocalDateTime predictEndTime;

    @ApiModelProperty("实际送达开始时间")
    private LocalDateTime practicalStartTime;

    @ApiModelProperty("实际送达结束时间")
    private LocalDateTime practicalEndTime;
}
