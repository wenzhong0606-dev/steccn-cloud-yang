package com.steccn.pms.vo;


import com.steccn.pms.dto.PmsMonthlyProductionPlanBomDTO;
import com.steccn.pms.dto.PmsMonthlyProductionPlanCraftDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PmsMonthlyProductionPlanVO {

    private Long id;

    private String materialName;

    private String materialCode;


    private String specification;

    @ApiModelProperty("厚度")
    private BigDecimal thickQuantity;

    @ApiModelProperty("厚度单位名称")
    private String thickUnitName;

    /**
     * 客户
     */
    private String client;

    private Long clientId;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 月度
     */
    private String monthly;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 计划数量
     */
    private BigDecimal plannedQuantity;

    /**
     * 已转数量
     */
    private BigDecimal transferredQuantity;

    /**
     * 完成数量
     */
    private BigDecimal completionQuantity;

    /**
     * 工艺版本
     */
    private String processVersion;

    private Long craftId;

    /**
     * 最小包装数量
     */
    private BigDecimal minimumPackingQuantity;


    private Long unitId;

    private String unitName;

    private Long minimumPackingQuantityUnit;

    private String minimumPackingQuantityUnitName;


    private List<PmsMonthlyProductionPlanBomVO> materialList;

    private List<PmsMonthlyProductionPlanCraftVO>  stationList;

}
