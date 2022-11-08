package com.steccn.pms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author chenhuan
 * @date 2022/11/7 11:00
 */
@Data
public class PmsPullForceEditDTO {

    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "班次id不能为空")
    @ApiModelProperty("班次id")
    private Long classesId;

    private Long id;

    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "产线id不能为空")
    @ApiModelProperty("产线id")
    private Integer productLineId;

    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "车间id不能为空")
    @ApiModelProperty("车间id")
    private Integer workshopId;

    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "日期不能为空")
    @ApiModelProperty("日期")
    private LocalDate pullForceDate;

    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "数量不能为空")
    @ApiModelProperty("数量")
    private BigDecimal quantity;
}
