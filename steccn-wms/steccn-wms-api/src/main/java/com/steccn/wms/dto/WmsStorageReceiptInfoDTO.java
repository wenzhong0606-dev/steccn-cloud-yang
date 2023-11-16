package com.steccn.wms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 入库凭证详情
 *
 * @author Mongknife
 * @date 2022/12/12
 */
@Data
@ApiModel("入库凭证详情DTO")
public class WmsStorageReceiptInfoDTO {
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("物料id")
//    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物料id不能为空")
    private Long materialId;

    @ApiModelProperty("物料编码")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物料编码不能为空")
    private String materialCode;

    @ApiModelProperty("物料名称")
//    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物料名称不能为空")
    private String materialName;

    @ApiModelProperty("货架id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "货架id不能为空")
    private Integer shelfId;

    @ApiModelProperty("货架编码")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "货架编码不能为空")
    private String shelfCode;

    @ApiModelProperty("货架名称")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "货架名称不能为空")
    private String shelfName;

    @ApiModelProperty("数量")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "数量不能为空")
    @DecimalMin(groups = {UpdateGroup.class, InsertGroup.class},value="0",message="数量应大于0")
    @DecimalMax(groups = {UpdateGroup.class, InsertGroup.class},value="99999999.999",message="数量大小应小于99999999.999")
    @Digits(groups = {UpdateGroup.class, InsertGroup.class},integer = 8,fraction = 3,message = "整数位上限为8位，小数位上限位3位")
    private BigDecimal quantity;

    @ApiModelProperty("关联单据")
    private String associatedNo;

    @ApiModelProperty("批次")
//    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "批次不能为空")
    private String batch;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("单位")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "单位不能为空")
    private Long unit;

    @ApiModelProperty("单位名称")
    private String unitName;

    @ApiModelProperty("有效期截至")
    private LocalDate validTime;

    @ApiModelProperty("金蝶收料通知单Id")
    private String erpLineId;

    @ApiModelProperty("外部系统订单头编号")
    private String esFromPoNum;

    @ApiModelProperty("外部系统订单行编号")
    private String esFromPoLineNum;

    @ApiModelProperty("产线名称（老线）")
    private String productionLineName;
}
