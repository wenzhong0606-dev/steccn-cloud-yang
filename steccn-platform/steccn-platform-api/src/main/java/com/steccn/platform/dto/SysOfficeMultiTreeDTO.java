package com.steccn.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 多棵树结构
 * @date 2023/8/11
 */
@Data
@ApiModel("多棵树结构请求对象")
public class SysOfficeMultiTreeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("根节点code集合")
    private List<String> rootCodeList;

    @ApiModelProperty("节点类型")
    private List<String> officeTypeList;
}
