package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 完工入库扫描vo
 */
@Data
@ApiModel("完工入库扫描vo")
public class CompleteStorageScanDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("箱状态")
    private Integer caseState;

    @ApiModelProperty("生产订单id")
    private Long produceOrderId;

    @ApiModelProperty("生产订单编号")
    private String produceOrderNo;

    @ApiModelProperty("仓库id")
    private Integer warehouseId;

    @ApiModelProperty("仓库名称")
    private String warehouseName;

    @ApiModelProperty("货架id")
    private Integer shelfId;

    @ApiModelProperty("货架名称")
    private String shelfName;

}
