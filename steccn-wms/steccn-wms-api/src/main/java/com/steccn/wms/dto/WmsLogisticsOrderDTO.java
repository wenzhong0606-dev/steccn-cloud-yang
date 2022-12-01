package com.steccn.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Mongknife
 * @date 2022/11/30
 */
@Data
@ApiModel("物流单DTO")
public class WmsLogisticsOrderDTO {

    @ApiModelProperty("物流单主键")
    private Long id;

    @ApiModelProperty("物流单编码")
    private String code;

    @ApiModelProperty("类型")
    private Long type;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("物流方")
    private Integer logisticsCompany;

    @ApiModelProperty("物流方名称")
    private String logisticsCompanyName;

    @ApiModelProperty("关联单据")
    private String associatedNo;

    @ApiModelProperty("预计送达时间")
    private LocalDateTime predictTime;

    @ApiModelProperty("实际送达时间")
    private LocalDateTime practicalTime;

    @ApiModelProperty("收货机构")
    private WmsLogisticsInfoDTO receiving;

    @ApiModelProperty("发货机构")
    private WmsLogisticsInfoDTO shipments;

    @ApiModelProperty("运输方式")
    private List<WmsLogisticsTransportationDTO> wayList;

    @ApiModelProperty("详情")
    private List<WmsLogisticsDetailDTO> list;
}
