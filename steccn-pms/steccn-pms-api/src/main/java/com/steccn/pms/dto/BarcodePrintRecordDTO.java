package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * @author yequan
 * @desc 打印记录dto
 */
@Data
@ApiModel("打印记录dto")
public class BarcodePrintRecordDTO extends BasePageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;

    @ApiModelProperty("批次编码")
    private String batchNo;

}
