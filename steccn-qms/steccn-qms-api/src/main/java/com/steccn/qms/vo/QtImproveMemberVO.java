package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 整改成员vo
 * @date 2023/3/15
 */
@Data
@ApiModel("整改成员vo")
public class QtImproveMemberVO implements Serializable {

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("OA的用户id")
    private Long userId;

    @ApiModelProperty("OA的真实名称")
    private String realName;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("是否在职 0否 1是")
    private Integer onJob;

    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("最后修改时间")
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;

}
