package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 生产订单-工艺信息-工位
 *
 * @author Mongknife
 * @date 2022/10/8
 */
@Data
@ApiModel("生产订单-工艺信息-工位")
public class PmsWorkOrderCraftStationVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("工位名称")
    private String executeStationName;


    @ApiModelProperty("工位编码")
    private String executeStationCode;

    @ApiModelProperty("工位Id")
    private Long executeStationId;
}
