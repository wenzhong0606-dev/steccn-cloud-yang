package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/10/11
 */
@Data
@ApiModel("生产订单批次信息")
public class PmsProduceOrderBatchDTO  extends BasePageDTO {

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;
}
