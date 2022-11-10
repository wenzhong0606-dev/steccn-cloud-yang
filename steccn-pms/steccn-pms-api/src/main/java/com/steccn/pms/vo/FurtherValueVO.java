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
@ApiModel("深加工value的视图对象")
@AllArgsConstructor
@NoArgsConstructor
public class FurtherValueVO implements Serializable {

    @ApiModelProperty("深加工valueStr的视图对象")
    private List<FurtherValueStrVO> value;

}
