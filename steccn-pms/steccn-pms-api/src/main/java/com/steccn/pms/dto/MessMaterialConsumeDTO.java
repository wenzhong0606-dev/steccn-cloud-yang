package com.steccn.pms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yequan
 */
@Data
@ApiModel("物料库存信息")
public class MessMaterialConsumeDTO implements Serializable {

    @ApiModelProperty("库存id")
    private Long inventoryId;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("可用数量")
    private BigDecimal availableQuantity;

    @ApiModelProperty("实际数量")
    private BigDecimal quantity;

    @ApiModelProperty("仓库code")
    private String warehouseCode;

    @ApiModelProperty("货架code")
    private String shelfCode;

    @ApiModelProperty("创建时间")
    private LocalDateTime createDate;

    @ApiModelProperty("打包时间")
    private LocalDateTime packTime;
}
