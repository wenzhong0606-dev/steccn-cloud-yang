package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 连线入库dto
 */
@Data
@ApiModel("连线入库dto")
public class ConnectLineStorageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("生产订单id")
    @NotNull(message = "生产订单不能为空")
    private Long produceOrderId;

    @ApiModelProperty("批次主键")
    @NotNull(message = "批次不能为空")
    private Long batch;

    @ApiModelProperty("数量")
    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0", message = "数量应大于0")
    @DecimalMax(value = "99999999.999", message = "数量大小应小于99999999.999")
    @Digits(integer = 8, fraction = 3, message = "整数位上限为8位，小数位上限位3位")
    private BigDecimal quantity;

    @ApiModelProperty("单位")
    @NotNull(message = "数量单位不能为空")
    private Long unitId;

    @ApiModelProperty("货架id")
    @NotNull(message = "货架不能为空")
    private Integer shelfId;

    @ApiModelProperty("班次id")
    @NotNull(message = "班次信息不能为空")
    private Long classesId;

    @ApiModelProperty("班组id")
    @NotNull(message = "班组信息不能为空")
    private Long classGroupId;

    @NotNull(message = "入库时间不能为空")
    @ApiModelProperty("入库时间")
    private LocalDateTime enterTime;

}
