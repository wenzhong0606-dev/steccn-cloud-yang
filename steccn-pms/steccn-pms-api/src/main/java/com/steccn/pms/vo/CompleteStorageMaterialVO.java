package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 完工入库-物料信息
 */
@Data
@ApiModel("完工入库-物料信息")
public class CompleteStorageMaterialVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("生产订单id")
    private Long workProduceId;

    @ApiModelProperty("组件id")
    private Integer moduleId;

    @ApiModelProperty("组件编码")
    private String moduleCode;

    @ApiModelProperty("组件名称")
    private String moduleName;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("单位")
    private Long unitId;

    @ApiModelProperty("单位")
    private String unitName;

    @ApiModelProperty("仓库id")
    private Integer warehouseId;

    @ApiModelProperty("仓库名称")
    private String warehouseName;

    @ApiModelProperty("货架id")
    private Integer shelfId;

    @ApiModelProperty("货架名称")
    private String shelfName;

}
