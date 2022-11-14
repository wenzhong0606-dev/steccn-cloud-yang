package com.steccn.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 机构类型
 *
 * @author Mongknife
 * @date 2022/9/6
 */
@Data
@ApiModel("机构类型vo")
public class SysOfficeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("类型编码")
    private String officeType;

    @ApiModelProperty("名称")
    private String officeName;

    @ApiModelProperty("编码")
    private String officeCode;

    @ApiModelProperty("本级排序号（升序）")
    private Integer treeSort;

    @ApiModelProperty("判断是否为最末级,0就是最末级")
    private String treeLeaf;

    @ApiModelProperty("排序")
    private Integer treeLevel;

    @ApiModelProperty("父级编码")
    private Integer parentId;

    @ApiModelProperty("父级编码")
    private String parentCode;

    @ApiModelProperty("父节点名称")
    private String parentName;

    @ApiModelProperty("子级节点id(根据该id 获取父级节点)")
    private Integer childrenId;

    @ApiModelProperty("子级节点")
    private List<SysOfficeVO> children;

}
