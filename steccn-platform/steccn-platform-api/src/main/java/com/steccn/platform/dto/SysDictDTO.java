package com.steccn.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 字典DTO
 *
 * @author Mongknife
 * @date 2022/9/14
 */
@Data
@ApiModel("字典DTO")
public class SysDictDTO {

    @ApiModelProperty("编码")
    private List<String> codeList;

    @ApiModelProperty("类型")
    private String type;
}
