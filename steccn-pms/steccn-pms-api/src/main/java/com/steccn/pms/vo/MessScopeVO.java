package com.steccn.pms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @desc mess接口权限字符串集合
 */
@Data
public class MessScopeVO implements Serializable {

    @ApiModelProperty("权限集合")
    private List<String> stringList;
}
