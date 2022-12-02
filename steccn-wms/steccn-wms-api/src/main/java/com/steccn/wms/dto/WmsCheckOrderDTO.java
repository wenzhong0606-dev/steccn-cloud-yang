package com.steccn.wms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 盘点单
 * @TableName wms_check_order
 */
@Data
public class WmsCheckOrderDTO  extends BasePageDTO implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("盘点单")
    private String code;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("类型")
    private Long type;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("批次")
    private String  batch;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("创建人名字")
    private String createName;

    @ApiModelProperty("盘点人名字")
    private String checkName;

    @ApiModelProperty("创建时间 时间区间（日期），根据单据创建时间介于区间搜索默认值：开始——当前日期减30天；结束——默认当前日期")
    private String createTimeTem;

    @ApiModelProperty("盘点开始时间 时间区间（日期），根据单据创建时间介于区间搜索  默认值：开始——当前日期减30天；结束——默认当前日期")
    private String checkTimeTem;

    private static final long serialVersionUID = 1L;
}