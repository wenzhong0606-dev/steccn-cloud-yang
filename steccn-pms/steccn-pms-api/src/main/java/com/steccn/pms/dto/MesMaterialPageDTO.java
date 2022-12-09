package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/8
 */
@Data
@ApiModel
public class MesMaterialPageDTO {

    @ApiModelProperty("货架名称")
    private String shelfName;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("批次")
    private String batch;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("页条数")
    private Integer pageSize;

    @ApiModelProperty("页码")
    private Integer pageNo;

}
