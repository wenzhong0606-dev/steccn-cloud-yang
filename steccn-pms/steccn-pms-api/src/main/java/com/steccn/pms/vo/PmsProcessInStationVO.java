package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/10/10
 */
@Data
@ApiModel("生产工序进站信息")
public class PmsProcessInStationVO {

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("进站时间")
    private LocalDateTime inStationTime;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("批次")
    private String batchNo;

    @ApiModelProperty("工位名称")
    private String  station;

    @ApiModelProperty ("工位id")
    private Integer stationId;



}
