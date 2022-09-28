package com.steccn.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @desc 数据字典vo
 */
@Data
@ApiModel("数据字典vo")
public class SysDictVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("类型编码")
    private String type;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("是否有效 0否1是")
    private Integer useFlag;

    @ApiModelProperty("父id")
    private Long parentId;

    @ApiModelProperty("孩子节点")
    private List<SysDictVO> children;
}
