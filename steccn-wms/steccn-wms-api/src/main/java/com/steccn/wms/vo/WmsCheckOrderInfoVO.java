package com.steccn.wms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 盘点单明细
 * @TableName wms_check_order_info
 */
@Data
public class WmsCheckOrderInfoVO implements Serializable {

    @ApiModelProperty("盘点明细主键")
    private Long id;

    @ApiModelProperty("仓库")
    private Integer warehouse;
    @ApiModelProperty("仓库名字")
    private String warehouseName;

    @ApiModelProperty("货架")
    private Integer shelf;
    @ApiModelProperty("货架名字")
    private String shelfName;

    @ApiModelProperty("物料id")
    private Long materialId;
    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("批次")
    private String batch;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("单位")
    private String unit;


    @ApiModelProperty("结果")
    private BigDecimal result;

    @ApiModelProperty("差额")
    private BigDecimal difference;

    @ApiModelProperty("盘点人id")
    private Long checkBy;

    @ApiModelProperty("盘点人名称")
    private String checkName;

    @ApiModelProperty("盘点时间")
    private LocalDateTime checkTime;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("状态（0：未执行 1：已执行）")
    private Integer status;


    @ApiModelProperty("盘点单id")
    private Long checkOrderId;

    @ApiModelProperty("创建人id")
    private Long createBy;

    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新人id")
    private Long updateBy;

    @ApiModelProperty("更新人名称")
    private String updateName;

    @ApiModelProperty("更新人时间")
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}