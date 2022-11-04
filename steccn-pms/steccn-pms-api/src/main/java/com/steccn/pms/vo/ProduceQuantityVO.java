package com.steccn.pms.vo;

import com.steccn.common.vo.BaseQuantityVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 */
@ApiModel("产量vo")
@Data
public class ProduceQuantityVO implements Serializable {

    @ApiModelProperty("数量vo")
    private List<BaseQuantityVO> quantityVOList;
}
