package com.steccn.wms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 物流单详情VO
 *
 * @author Mongknife
 * @date 2022/11/30
 */
@Data
@ApiModel("物流单详情VO")
public class WmsLogisticsDetailDTO extends BasePageDTO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("物流单id")
    private Long logisticsId;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("发货数量")
    private BigDecimal shippedQuantity;

    @ApiModelProperty("到货数量")
    private BigDecimal aogQuantity;

    /**
     * 重量
     */
    @ApiModelProperty("重量")
    private BigDecimal weight;

    /**
     * 体积
     */
    @ApiModelProperty("体积")
    private BigDecimal volume;

    /**
     * 重量单位
     */
    @ApiModelProperty("重量单位")
    private Long weightUnit;

    @ApiModelProperty("重量单位名称")
    private Long weightUnitName;

    /**
     * 体积单位
     */
    @ApiModelProperty("体积单位")
    private Long volumeUnit;

    @ApiModelProperty("体积单位名称")
    private Long volumeUnitName;

    /**
     * 关联单据
     */
    @ApiModelProperty("关联单据")
    private String associatedNo;

    /**
     * 是否拆包装（0：否  1：是）
     */
    @ApiModelProperty("是否拆包装")
    private Integer unpackFlag;

    /**
     * 包装数量
     */
    @ApiModelProperty("包装数量")
    private BigDecimal packQuantity;

    /**
     * 单位包装数量
     */
    @ApiModelProperty("单位包装数量")
    private BigDecimal unitPackQuantity;


}
