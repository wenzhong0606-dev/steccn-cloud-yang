package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产订单进站信息VO
 *
 * @author Mongknife
 * @date 2022/10/10
 */
@Data
@ApiModel("生产订单进站信息VO")
public class PmsCompleteDeliveryVO {

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("出库单号")
    private String code;

    @ApiModelProperty("出库时间")
    private LocalDateTime deliveryTime;

    @ApiModelProperty("状态")
    private Integer state;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("批次")
    private String batchNo;

    @ApiModelProperty("仓库")
    private String warehouseName;

    @ApiModelProperty("货架")
    private String shelfName;

    @ApiModelProperty("工序")
    private String processName;
}
