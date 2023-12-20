package com.steccn.platform.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 标准产能的明细
 * @date 2023/12/20
 */
@Data
public class SysProductionCapacityInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Integer id;

    /**
     * 车间
     */
    private Integer workshopId;

    private String workshopName;

    /**
     * 产线
     */
    private Integer lineId;

    private String lineName;

    /**
     * 工位
     */
    private Integer stationId;

    private String stationName;

    /**
     * 产线基数
     */
    private Integer lineNum;

    /**
     * 产能主键
     */
    private Integer capacityId;

    /**
     * 产能
     */
    private BigDecimal standardCapacity;

}
