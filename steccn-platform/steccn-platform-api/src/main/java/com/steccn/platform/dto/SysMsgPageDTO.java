package com.steccn.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * @author yequan
 * @desc 系统消息分页dto
 */
@Data
@ApiModel("消息中心分页dto")
public class SysMsgPageDTO extends SlidePageDTO {

    @ApiModelProperty("消息类型集合")
    @NotNull(message = "消息分类不能为空")
    private Integer category;
}
