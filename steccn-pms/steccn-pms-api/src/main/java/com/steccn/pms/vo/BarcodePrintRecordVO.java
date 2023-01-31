package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 打印记录明细信息
 */
@Data
@ApiModel("打印记录明细信息")
public class BarcodePrintRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("时间")
    private LocalDateTime createTime;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("最小包装数量")
    private BigDecimal minPackageQuantity;

    @ApiModelProperty("箱子状态")
    private Integer caseState;

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;
}
