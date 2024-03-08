package com.steccn.ems.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 折标系数-新增编辑对象
 */
@Data
public class EpDiscountFactorDTO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 主键 - 编辑时使用
     */
    private Integer id;

    /**
     * 系数名称
     */
    private String name;

    /**
     * 系数类型
     */
    private Long type;

    /**
     * 能源类型
     */
    private Integer energyType;

    /**
     * 系数值
     */
    private BigDecimal value;

    /**
     * 计量单位
     */
    private Long valueUnit;

    /**
     * 有效期
     */
    private LocalDate validStart;

    /**
     * 有效期
     */
    private LocalDate validEnd;

    /**
     * 备注
     */
    private String note;
}
