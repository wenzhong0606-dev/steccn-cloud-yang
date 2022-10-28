package com.steccn.pms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 生产订单工艺信息DTO
 *
 * @author Mongknife
 * @date 2022/10/8
 */
@ApiModel("生产订单工艺信息DTO")
@Data
public class PmsProduceOrderCraftDTO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("工序编码")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "工序编码不能为空")
    private String code;

    @ApiModelProperty("工序名称")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "工序名称不能为空")
    private String name;

    @ApiModelProperty("工序id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "工序名称不能为空")
    private String processId;

    @ApiModelProperty("类型id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "类型不能为空")
    private Long typeId;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("标准数量")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "标准数量不能为空")
    private BigDecimal standardQuantity;

    @ApiModelProperty("单位Id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "单位不能为空")
    private Long unitId;

    @ApiModelProperty("是否报工")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "是否报工不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer reportWorkFlag;

    @ApiModelProperty("是否报检")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "是否报检不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer reportCheckFlag;

    @ApiModelProperty("进站")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "进站信息不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer inStationFlag;

    @ApiModelProperty("出站")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "出站信息不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer outStationFlag;

    @ApiModelProperty("工位信息")
    private List<PmsWorkOrderCraftStationDTO> stationList;
}
