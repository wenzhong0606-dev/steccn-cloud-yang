package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 生产订单工艺信息DTO
 *
 * @author Mongknife
 * @date 2022/10/8
 */
@ApiModel("生产订单工艺信息DTO")
@Data
public class PmsProduceOrderCraftDTO {

    @ApiModelProperty("工序编码")
    private String code;

    @ApiModelProperty("工序名称")
    private String name;

    @ApiModelProperty("类型id")
    private String typeId;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("标准数量")
    private BigDecimal standardQuantity;

    @ApiModelProperty("单位Id")
    private Long unitId;

    @ApiModelProperty("是否报工")
    private Integer reportWorkFlag;

    @ApiModelProperty("是否报检")
    private Integer reportCheckFlag;

    @ApiModelProperty("进站")
    private Integer inStationFlag;

    @ApiModelProperty("出站")
    private Integer outStationFlag;

    @ApiModelProperty("工位信息")
    private List<PmsWorkOrderCraftStationDTO> stationList;
}
