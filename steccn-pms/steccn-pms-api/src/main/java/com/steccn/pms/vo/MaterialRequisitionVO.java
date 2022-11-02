package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 生产领料单VO
 *
 * @author Mongknife
 * @date 2022/11/2
 */
@Data
@ApiModel("生产领料单VO")
public class MaterialRequisitionVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("关联单号")
    private String associatedNo;

    @ApiModelProperty("部门id")
    private Long departmentId;

    @ApiModelProperty("部门名称")
    private String departmentName;

    @ApiModelProperty("状态id")
    private Long status;

    @ApiModelProperty("状态名称")
    private String statusName;

    @ApiModelProperty("类型id")
    private String typeId;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("创建时间")
    private String createTime;
}
