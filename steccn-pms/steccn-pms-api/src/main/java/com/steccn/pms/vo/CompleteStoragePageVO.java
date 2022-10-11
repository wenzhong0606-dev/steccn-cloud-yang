package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yequan
 * @desc 完工入库分页vo
 */
@Data
@ApiModel("完工入库详细信息vo")
public class CompleteStoragePageVO implements Serializable {

    @ApiModelProperty("入库单id")
    private Long id;

    @ApiModelProperty("入库单编码")
    private String code;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("单位")
    private Long unitId;

    @ApiModelProperty("单位")
    private String unitName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("入库时间")
    private LocalDateTime inStorageTime;

    @ApiModelProperty("创建人")
    private String createName;

    @ApiModelProperty("确认人")
    private String confirmName;

    @ApiModelProperty("类型")
    private Integer type;
}
