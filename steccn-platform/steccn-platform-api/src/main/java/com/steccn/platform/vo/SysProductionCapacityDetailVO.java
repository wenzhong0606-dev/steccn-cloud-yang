package com.steccn.platform.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 标准产能对外暴露的接口
 * @date 2023/12/28
 */
@Data
public class SysProductionCapacityDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 开始日期
     */
    private LocalDate start;

    /**
     * 结束日期
     */
    private LocalDate end;

    /**
     * 标准产能
     */
    private BigDecimal standardCapacity;

    /**
     * 产线基数
     */
    private Integer lineNum;

    /**
     * 车间主键
     */
    private Integer workshopId;

    /**
     * 产线主键
     */
    private Integer lineId;

    /**
     * 工位主键
     */
    private Integer stationId;

    /**
     * 工位的产线基数
     */
    private Integer stationLineNum;

    /**
     * 工位的标准产能
     */
    private BigDecimal stationStandardCapacity;

}
