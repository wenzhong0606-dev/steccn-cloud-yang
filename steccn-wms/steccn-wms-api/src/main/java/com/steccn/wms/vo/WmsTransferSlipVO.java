package com.steccn.wms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/11/30
 */
@Data
@ApiModel("调拨单VO")
public class WmsTransferSlipVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("源仓库")
    private Integer sourceWarehouse;

    @ApiModelProperty("源仓库名称")
    private String sourceWarehouseName;

    @ApiModelProperty("调拨单号")
    private String code;

    @ApiModelProperty("类型")
    private Long type;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("创建人")
    private String createName;

    @ApiModelProperty("确认人")
    private String affirmName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("确认时间")
    private LocalDateTime affirmTime;

    @ApiModelProperty("备注")
    private String note;

}
