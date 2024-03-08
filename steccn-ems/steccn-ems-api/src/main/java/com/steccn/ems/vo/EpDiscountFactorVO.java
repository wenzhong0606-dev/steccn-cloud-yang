package com.steccn.ems.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 折标系数视图对象
 */
@Data
public class EpDiscountFactorVO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Integer id;

    /**
     * 系统编码
     */
    private String code;

    /**
     * 系数名称
     */
    private String name;

    /**
     * 系数类型 当量折标系数，等价折标系数，窑龄系数
     */
    private Long type;

    /**
     * 系数类型 当量折标系数，等价折标系数，窑龄系数
     */
    private String typeName;

    /**
     * 能源类型
     */
    private Integer energyType;

    /**
     * 能源类型
     */
    private String energyTypeName;

    /**
     * 系数值
     */
    private BigDecimal value;

    /**
     * 计量单位
     */
    private Long valueUnit;

    /**
     * 计量单位
     */
    private String valueUnitName;

    /**
     * 有效期-开始
     */
    private LocalDate validStart;

    /**
     * 有效期-结束
     */
    private LocalDate validEnd;

    /**
     * 备注
     */
    private String note;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
