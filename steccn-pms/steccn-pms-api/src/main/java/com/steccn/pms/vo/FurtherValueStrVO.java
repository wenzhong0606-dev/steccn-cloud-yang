package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("深加工valueStr的视图对象")
public class FurtherValueStrVO implements Serializable {

    @ApiModelProperty("value的值")
    private List<String> value;
}
