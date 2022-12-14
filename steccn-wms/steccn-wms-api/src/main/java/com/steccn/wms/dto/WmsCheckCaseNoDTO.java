package com.steccn.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/14
 */
@Data
@ApiModel("校验箱号dto")
public class WmsCheckCaseNoDTO {

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("货架id")
    private Integer shelfId;

    @ApiModelProperty("批次")
    private String batch;

    @ApiModelProperty("物料id")
    private Long  materialId;
}
