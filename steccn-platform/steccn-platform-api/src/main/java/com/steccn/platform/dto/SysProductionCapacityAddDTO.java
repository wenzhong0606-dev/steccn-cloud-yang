package com.steccn.platform.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 新增标准产能
 * @date 2023/12/20
 */
@Data
public class SysProductionCapacityAddDTO implements Serializable {

    /**
     * 日期
     */
    private LocalDate classDate;

    /**
     * 标准产能
     */
    private BigDecimal standardCapacity;

    /**
     * 产线基数
     */
    private Integer lineNum;

}

