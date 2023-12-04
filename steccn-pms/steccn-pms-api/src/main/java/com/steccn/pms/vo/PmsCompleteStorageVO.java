package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/10/9
 */
@Data
@ApiModel
public class PmsCompleteStorageVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("入库单号")
    private String code;

    @ApiModelProperty("入库时间")
    private LocalDateTime inStorageTime;

    @ApiModelProperty("状态")
    private Integer state;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("批次")
    private String batchNo;

    @ApiModelProperty("仓库id")
    private Integer warehouseId;

    @ApiModelProperty("仓库")
    private String warehouseName;

    @ApiModelProperty("货架Id")
    private Integer shelfId;

    @ApiModelProperty("货架")
    private String shelfName;

    @ApiModelProperty("是否已打印")
    private Integer printFlag;

    @ApiModelProperty("打印次数")
    private Integer printCount;

    @ApiModelProperty("产线id")
    private Integer productLineId;
}
