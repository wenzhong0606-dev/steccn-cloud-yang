package com.steccn.pms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 领料单审批DTO
 *
 * @author Mongknife
 * @date 2022/11/3
 */
@Data
@ApiModel("领料单审批DTO")
public class MaterialRequisitionApprovalDTO {

    @ApiModelProperty("主键")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "领料单id不能为空")
    private Long id;

    @ApiModelProperty("审批是否通过")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "审批状态不能为空")
    @Max(groups = {UpdateGroup.class, InsertGroup.class},value = 1, message = "状态不正确 0：通过，1：未通过")
    @Min(groups = {UpdateGroup.class, InsertGroup.class},value = 0, message = "状态不正确 0：通过，1：未通过")
    private Integer approvalFlag;

    @ApiModelProperty("流程节点id")
    private String nodeId;
}
