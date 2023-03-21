package com.steccn.ems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 能耗分区树型结构
 * @date 2023/3/21
 */
@Data
@ApiModel("能耗分区树型结构")
public class EpPartitionTreeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("组织机构id")
    private Integer id;

    @ApiModelProperty("组织机构名称")
    private String officeName;

    @ApiModelProperty("排序字段")
    private Integer sort;

    @ApiModelProperty("孩子节点")
    private List<EpPartitionListDTO> children;
}
