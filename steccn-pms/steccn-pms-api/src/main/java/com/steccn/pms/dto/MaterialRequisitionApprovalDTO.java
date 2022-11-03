package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
    private Long id;

    @ApiModelProperty("审批是否通过")
    private Integer approvalFlag;

}
