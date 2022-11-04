package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 生产领料单VO
 *
 * @author Mongknife
 * @date 2022/11/2
 */
@Data
@ApiModel("生产领料单VO")
public class MaterialRequisitionVO implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("关联单号")
    private String associatedNo;

    @ApiModelProperty("部门id")
    private Integer departmentId;

    @ApiModelProperty("仓库id")
    private Integer warehouseId;

    @ApiModelProperty("仓库名称")
    private String warehouseName;

    @ApiModelProperty("部门名称")
    private String departmentName;

    @ApiModelProperty("状态id")
    private Long status;

    @ApiModelProperty("状态名称")
    private String statusName;

    @ApiModelProperty("类型id")
    private Long typeId;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("创建时间")
    private String createTime;

    @ApiModelProperty("详情信息")
    private List<MaterialRequisitionInfoVO> list;
}
