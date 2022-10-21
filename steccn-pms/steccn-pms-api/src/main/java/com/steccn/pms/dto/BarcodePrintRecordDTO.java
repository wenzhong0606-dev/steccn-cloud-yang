package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
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
    @NotNull(message = "生产订单主键不能为空")
    private Long produceOrderId;

    @ApiModelProperty("批次编码")
    @NotNull(message = "生产订单批次号不能为空")
    private String batchNo;

    @ApiModelProperty("状态")
    private Integer caseState;


}
