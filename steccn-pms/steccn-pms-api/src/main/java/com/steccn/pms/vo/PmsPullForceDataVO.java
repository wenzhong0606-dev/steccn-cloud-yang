package com.steccn.pms.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 拉引量VO
 *
 * @author AndrewLuo
 * @date 2023/07/07
 */
@Data
public class PmsPullForceDataVO implements Serializable {
    /**
     * 日期
     */
    private LocalDate pullForceDate;

    /**
     * 车间id
     */
    private Integer workshopId;

    /**
     * 产线id
     */
    private Integer productLineId;

    /**
     * 班次id
     */
    private Long classesId;

    /**
     * 拉引量
     */
    private BigDecimal quantity;

    /**
     * 单位id
     */
    private Long unitId;

}
