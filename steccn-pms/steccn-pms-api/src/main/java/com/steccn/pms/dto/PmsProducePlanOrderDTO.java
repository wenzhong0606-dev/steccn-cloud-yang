package com.steccn.pms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 生产月计划下达DTO
 *
 * @author Mongknife
 * @date 2022/11/4
 */
@Data
@ApiModel("生产月计划下达DTO")
public class PmsProducePlanOrderDTO {

    @ApiModelProperty("计划月份(yyyy-MM)")
    @NotNull(groups = {UpdateGroup.class}, message = "计划下达月份不能为空")
    private String planDate;
    @ApiModelProperty("物料id")
    @NotNull(groups = {UpdateGroup.class}, message = "物料不能为空")
    private List<Long> materialIds;
}
