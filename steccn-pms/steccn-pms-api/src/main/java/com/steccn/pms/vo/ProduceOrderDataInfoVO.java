package com.steccn.pms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProduceOrderDataInfoVO {

    /**
     * 产线名称
     */
    private String productionLineName;

    /**
     * 产线唯一标识
     */
    private Integer productionLineId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 物料唯一标识
     */
    private Long materialId;

    /**
     * 物料规格
     */
    private String specification;


    /**
     * 计划数量
     */
    private BigDecimal planQuantity;

    /**
     * 当前产量
     */
    private BigDecimal currentProduction;


    /**
     * 单位id
     */
    private Long unitId;


    /**
     * 单位名称
     */
    private String unitName;


    /**
     * 计划开始时间
     */
    private LocalDateTime planStartTime;

    /**
     * 计划结束时间
     */
    private LocalDateTime planEndTime;


    /**
     * 产线标识:(0:基片 1：深加工)
     */
    private Integer lineFlag = 1;

    /**
     * 车间
     */
    private Integer workNumberFlag;

    /**
     * 产线
     */
    private Integer lineNumberFlag;


    /**
     * 长度单位
     */
    private String lengthUnitName;

    /**
     * 长度
     */
    private BigDecimal lengthQuantity;


    /**
     * 高度单位
     */
    private String highUnitName;

    /**
     * 高度
     */
    private BigDecimal highQuantity;

    /**
     * 宽度单位
     */
    private String wideUnidName;

    /**
     * 宽度
     */
    private BigDecimal wideQuantity;


}
