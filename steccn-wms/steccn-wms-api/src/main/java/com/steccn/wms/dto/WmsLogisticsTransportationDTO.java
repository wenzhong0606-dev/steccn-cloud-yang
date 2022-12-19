package com.steccn.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/11/30
 */
@ApiModel("运输方式VO")
@Data
public class WmsLogisticsTransportationDTO {
    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("运输方式")
    private String way;

    @ApiModelProperty("分类id")
    private Long classificationId;

}
