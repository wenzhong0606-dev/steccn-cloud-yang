package com.steccn.wms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/12
 */
@Data
@ApiModel("入库凭证VO")
public class WmsStorageReceiptInfoVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("物料规格")
    private String specification;

    @ApiModelProperty("物料分类")
    private String classificationName;

    @ApiModelProperty("货架id")
    private Long shelfId;

    @ApiModelProperty("货架编码")
    private String shelfCode;

    @ApiModelProperty("货架名称")
    private String shelfName;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("关联单据")
    private String associatedNo;

    @ApiModelProperty("批次")
    private String batch;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("单位")
    private Long unit;

    @ApiModelProperty("单位名称")
    private String unitName;

    @ApiModelProperty("有效期截至")
    private LocalDate validTime;
}
