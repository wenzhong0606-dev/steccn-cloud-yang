package com.steccn.platform.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author yequan
 * @desc 系统消息分页dto
 */
@Data
@ApiModel("消息中心分页dto")
public class SysMsgPageDTO extends BasePageDTO {

    @ApiModelProperty("消息类型集合")
    private Integer category;
}
