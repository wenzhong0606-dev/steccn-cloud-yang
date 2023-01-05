package com.steccn.data.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 数仓-质量管理-缺陷vo
 */
@ApiModel("数仓-质量管理-缺陷vo")
@Data
public class DwDefectCollectVO implements Serializable {

    @ApiModelProperty("线次")
    private String line;

    @ApiModelProperty("缺陷种类")
    private String defectType;

    @ApiModelProperty("缺陷等级")
    private String level;

    @ApiModelProperty("缺陷数量")
    private Integer defectCount;

    @ApiModelProperty("主表id")
    private Long pushId;

    @ApiModelProperty("创建时间")
    private LocalDateTime productTime;

}
