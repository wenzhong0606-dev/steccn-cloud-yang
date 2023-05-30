package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 完工入库信息DTO
 *
 * @author Mongknife
 * @date 2022/10/9
 */
@Data
@ApiModel("完工入库信息DTO")
public class PmsCompleteStorageDTO extends BasePageDTO {

    private Long id;

    @ApiModelProperty("订单id")
    private Long produceOrderId;

    @ApiModelProperty("入库单编码")
    private String code;

    @ApiModelProperty("确认人名称")
    private String confirmName;
}
