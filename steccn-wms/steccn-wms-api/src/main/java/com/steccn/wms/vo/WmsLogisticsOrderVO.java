package com.steccn.wms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流单VO
 *
 * @author Mongknife
 * @date 2022/11/30
 */
@ApiModel("物流单VO")
@Data
public class WmsLogisticsOrderVO {

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
    private WmsLogisticsInfoVO receiving;

    @ApiModelProperty("发货机构")
    private WmsLogisticsInfoVO shipments;

    @ApiModelProperty("运输方式")
    private List<WmsLogisticsTransportationVO> wayList;

    @ApiModelProperty("详情")
    private List<WmsLogisticsDetailVO> list;

    @ApiModelProperty("创建人")
    private String createName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("备注")
    private String note;
}