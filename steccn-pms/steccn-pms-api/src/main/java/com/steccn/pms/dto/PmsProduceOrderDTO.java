package com.steccn.pms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产订单DTO
 *
 * @author Mongknife
 * @date 2022/10/8
 */
@Data
@ApiModel("生产订单")
public class PmsProduceOrderDTO {

    @ApiModelProperty("主键id")
    @NotNull(groups = UpdateGroup.class, message = "编辑数据时主键不能为空")
    private Long id;

    @ApiModelProperty("计划转生产标识")
    private Integer planFlag;

    @ApiModelProperty("Mrp主键")
    private Long mrpId;

    @ApiModelProperty("类型id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "类型不能为空")
    private Long typeId;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("物料id")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "物料id不能为空")
    private Long materialId;

    @ApiModelProperty("物料名称")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "物料名称不能为空")
    private String materialName;

    @ApiModelProperty("物料名称")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "物料名称不能为空")
    private String materialCode;

    @ApiModelProperty("计划开始时间")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "计划开始时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime planStartTime;

    @ApiModelProperty("计划结束时间")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "计划结束时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime planEndTime;

    @ApiModelProperty("数量")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "数量不能为空")
    @DecimalMin(groups = {UpdateGroup.class, InsertGroup.class}, value = "0", message = "数量应大于0")
    @DecimalMax(groups = {UpdateGroup.class, InsertGroup.class}, value = "99999999.999", message = "数量大小应小于99999999.999")
    @Digits(groups = {UpdateGroup.class, InsertGroup.class}, integer = 8, fraction = 3, message = "整数位上限为8位，小数位上限位3位")
    private BigDecimal quantity;

    @ApiModelProperty("单位id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "单位不能为空")
    private Long unitId;

    @ApiModelProperty("产线id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "产线不能为空")
    private Integer productionLineId;

    @ApiModelProperty("状态")
    private Integer state;

    @ApiModelProperty("工艺id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "工艺不能为空")
    private Long craftId;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("默认货架id")
    private Integer defaultShelfId;

    @ApiModelProperty("最小包装数量")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "最小包装数量不能为空")
    @DecimalMin(groups = {UpdateGroup.class, InsertGroup.class}, value = "0", message = "最小包装数量应大于0")
    @DecimalMax(groups = {UpdateGroup.class, InsertGroup.class}, value = "99999999.999", message = "最小包装数量大小应小于99999999.999")
    @Digits(groups = {UpdateGroup.class, InsertGroup.class}, integer = 8, fraction = 3, message = "最小包装数量整数位上限为8位，小数位上限位3位")
    private BigDecimal minPackageQuantity;

    @ApiModelProperty("最小包装数量单位")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "最小包装数量单位不能为空")
    private BigDecimal minPackageUnitId;

    @ApiModelProperty("工序信息")
    @Valid
    private List<PmsProduceOrderCraftDTO> processList;

    @Valid
    @ApiModelProperty("物料信息")
    private List<PmsWorkOrderBomDTO> materialList;
}
