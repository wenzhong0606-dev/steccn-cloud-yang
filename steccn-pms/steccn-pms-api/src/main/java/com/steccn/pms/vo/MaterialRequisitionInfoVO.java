package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 领料单详情VO
 *
 * @author Mongknife
 * @date 2022/11/2
 */
@Data
@ApiModel("领料单详情VO")
public class MaterialRequisitionInfoVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("关联单号")
    private String associatedNo;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("机构id")
    private List<Long> organization;

    @ApiModelProperty("机构名称")
    private List<String> organizationName;

    @ApiModelProperty("需求数量")
    private String quantity;

    @ApiModelProperty("领用数量")
    private String receiveQuantity;

    @ApiModelProperty("需求时间")
    private String requireDate;

    @ApiModelProperty("货架id")
    private Long shelfId;

    @ApiModelProperty("货架名称")
    private String shelfName;

    @ApiModelProperty("单位id")
    private String unitId;

    @ApiModelProperty("单位数量")
    private String unitName;
}
