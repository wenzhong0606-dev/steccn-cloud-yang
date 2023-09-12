package com.steccn.wms.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

@Data
@ApiModel("入库信息标识VO")
public class WmsStorageFlagInfoVO {


    @ApiModelProperty("箱号是否在平台存在")
    private Integer caseNoFlag = 0;

    @ApiModelProperty("箱号是否关联入库单")
    private Integer relevancyFlag = 0;

    @ApiModelProperty("箱号是否关联入库单")
    private Integer orderStateCreateFlag = 0;

    @ApiModelProperty("入库单状态是否为已确认/已完成")
    private Integer orderStateAffirmFlag = 0;

}
