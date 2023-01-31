package com.steccn.qms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 首检对象视图
 *
 * @author AndrewLuo
 * @date 2023/01/04
 */
@Data
public class FurtherValueObjVO implements Serializable {
    @ApiModelProperty("value的值")
    private List<ColorValueVO> value;
}
