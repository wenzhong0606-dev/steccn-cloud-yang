package com.steccn.pms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 工艺路线工序DTO
 *
 * @author Mongknife
 * @date 2022/9/7
 */
@Data
@ApiModel("工艺路线工序DTO")
public class PmCraftRouteProcessDTO implements Serializable {


    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("工序名称")
    private String processName;

    @ApiModelProperty("工艺路线id")
    private Long routeId;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("数量单位")
    private Long unitId;

    @ApiModelProperty("标准数量")
    private BigDecimal standardQuantity;

    @ApiModelProperty("标准数量单位")
    private Long standardUnitId;


    @ApiModelProperty("是否报工")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "是否报工不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer reportWorkFlag;

    @ApiModelProperty("是否报检")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "是否报检不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer reportCheckFlag;


    @ApiModelProperty("是否进站")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "是否进站不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer inStationFlag;


    @ApiModelProperty("是否出站")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "是否出站不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer outStationFlag;


    @ApiModelProperty("工序报废百分比")
    @NotBlank(groups = {InsertGroup.class, UpdateGroup.class}, message = "工序报废百分比不能为空")
    @Max(value = 100, message = "百分比不能大于100")
    @Min(value = 0, message = "百分比不能小于0")
    private BigDecimal processScrap;

}
