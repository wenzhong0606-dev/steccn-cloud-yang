package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class PmsMonthlyProductionPlanDTO extends BasePageDTO {

    private Long id;

    /**
     * 客户
     */
    private String client;

    private Integer clientId;

    /**
     * 物料id
     */
    private Long materialId;

    private String materialName;

    private String materialCode;
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

    private Long minimumPackingQuantityUnit;



    @Valid
    @ApiModelProperty("物料信息")
    private List<PmsMonthlyProductionPlanBomDTO>  materialList;


    @Valid
    @ApiModelProperty("工艺信息")
    private List<PmsMonthlyProductionPlanCraftDTO>  stationList;


    private Long typeId;

    private BigDecimal quantity;

    private LocalDateTime planStartTime;

    private LocalDateTime planEndTime;

    private Integer productionLineId;

    @ApiModelProperty("工序信息")
    @Valid
    private List<PmsProduceOrderCraftDTO> processList;

    @Valid
    @ApiModelProperty("物料信息")
    private List<PmsWorkOrderBomDTO>    bomList;


}
