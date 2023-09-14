package com.steccn.wms.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 销售系统入库单生成物料明细
 *
 * @author AndrewLuo
 * @date 2023/09/13
 */
@Data
public class WmsStorageSalesMaterialDTO {
    @NotEmpty(message = "物料编码不能为空")
    private String materialCode;

    @ApiModelProperty("批次默认为0")
    private String batch = "0";

    @NotNull(message = "包装数不能为空")
    private Integer packagePackNum;

    @NotNull(message = "净重不能为空")
    private BigDecimal netWeight;

    @NotEmpty(message = "关联单据不能为空")
    private String associatedNo;

    @NotNull(message = "货架编码不能为空")
    private String shelfCode;

    @NotEmpty(message = "单位名称不能为空")
    private String unitName;

}
