package com.steccn.ems.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 单耗班组明细
 */
@ApiModel("单耗班组明细")
@Data
public class ConsumePartitionUnitClassDetailVO extends EpDetailVO implements Serializable {

    @ApiModelProperty("分区节点名称")
    private String partitionName;

    @ApiModelProperty("班组名称")
    private String classGroupName;

    @ApiModelProperty("单耗")
    private String unitValueStr;

}
