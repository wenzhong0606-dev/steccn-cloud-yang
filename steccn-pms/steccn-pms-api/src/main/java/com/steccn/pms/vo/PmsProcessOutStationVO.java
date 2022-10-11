package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产订单出站VO
 *
 * @author Mongknife
 * @date 2022/10/11
 */
@Data
@ApiModel("生产订单出站VO")
public class PmsProcessOutStationVO {


    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("出站时间")
    private LocalDateTime outStationTime;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("批次")
    private String batchNo;

    @ApiModelProperty("工位")
    private String station;

    @ApiModelProperty("工位id")
    private Integer stationId;
}
