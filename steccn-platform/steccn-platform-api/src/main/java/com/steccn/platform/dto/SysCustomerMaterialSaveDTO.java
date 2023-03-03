package com.steccn.platform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author yequan
 * @desc 客户物料保存dto
 * @date 2023/3/3
 */
@Data
@ApiModel("客户物料保存dto")
public class SysCustomerMaterialSaveDTO implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("物料主键")
    private Long materialId;

    @ApiModelProperty("最小包装数量")
    private BigDecimal minPackQuantity;

    @ApiModelProperty("客户主键")
    private Integer customerId;

    @ApiModelProperty("打印模板主键")
    private Long printModel;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("明细数据")
    private List<SysCustomerMaterialSubDTO> subVOList;
}
