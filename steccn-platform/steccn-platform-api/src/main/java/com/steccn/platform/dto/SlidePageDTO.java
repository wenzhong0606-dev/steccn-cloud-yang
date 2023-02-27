package com.steccn.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author yequan
 * @desc 滑动分页dto
 */
@Data
@ApiModel("滑动分页SlidePageDTO")
@AllArgsConstructor
@NoArgsConstructor
public class SlidePageDTO implements Serializable {

    @ApiModelProperty("下标")
    private Long idx;

    @ApiModelProperty("一页几条")
    private Integer size;

}
