package com.steccn.wms.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仓储驾驶仓单据VO
 * @author 孟乐
 * @date 2023年11月23日
 */

@Data
@ApiModel("仓储驾驶仓单据VO")
public class WmsReportWarehouseVO {

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("类型")
    private Long type;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("确认人")
    private String affirmName;

    @ApiModelProperty("确认时间")
    private LocalDateTime affirmTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("创建名称")
    private String createName;


}
