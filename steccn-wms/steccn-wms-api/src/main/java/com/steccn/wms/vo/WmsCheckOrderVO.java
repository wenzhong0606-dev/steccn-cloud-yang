package com.steccn.wms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 盘点单
 * @TableName wms_check_order
 */
@Data
public class WmsCheckOrderVO implements Serializable {

    @ApiModelProperty("盘点主键")
    private Long id;

    @ApiModelProperty("盘点单号")
    private String code;

    @ApiModelProperty("盘点名称")
    private String name;

    @ApiModelProperty("类型")
    private Long type;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("状态  0-已创建 1-执行中 2-已完成 3-已关闭")
    private Integer status;

    @ApiModelProperty("状态名称")
    private String statusName;

    @ApiModelProperty("盘点岗位")
    private Integer post;

    @ApiModelProperty("盘点岗位名称")
    private String postName;

    @ApiModelProperty("盘点开始时间")
    private LocalDateTime checkTime;

    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("盘点明细")
    private List<WmsCheckOrderInfoVO> wmsCheckOrderInfoVOList;
    @ApiModelProperty("盘点区域")
    private Object checkArea;
    private static final long serialVersionUID = 1L;
}