package com.steccn.platform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yequan
 * @desc 客户物料分页vo
 * @date 2023/3/3
 */
@ApiModel("客户物料分页vo")
@Data
public class SysCustomerMaterialVO implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("物料主键")
    private Long materialId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("最小包装数量")
    private BigDecimal minPackQuantity;

    @ApiModelProperty("客户主键")
    private Integer customerId;

    @ApiModelProperty("客户名称")
    private String customerName;

    @ApiModelProperty("打印模板主键")
    private Long printModel;

    @ApiModelProperty("打印模板名称")
    private String printModelName;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("创建人")
    private String createName;

    @ApiModelProperty("创建人名称")
    private LocalDateTime createTime;

    @ApiModelProperty("明细数据")
    private List<SysCustomerMaterialSubVO> subVOList;
}
