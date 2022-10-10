package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/10/10
 */
@Data
@ApiOperation("投料信息Dto")
public class PmsCompleteDeliveryDTO extends BasePageDTO {

    @ApiModelProperty("订单id")
    private Long produceOrderId;

}
