package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 * @desc
 * @date 2023/3/15
 */
@ApiModel("整改成员")
@Data
public class QtImproveMemberDTO implements Serializable {

    @ApiModelProperty("OA的用户id")
    private Long userId;

    @ApiModelProperty("OA的真实名称")
    private String realName;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("是否在职 0否 1是")
    private Integer onJob;
}
