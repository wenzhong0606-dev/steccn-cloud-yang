package com.steccn.ems.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 电力设置视图对象
 */
@Data
public class CeElectricSettingVO implements Serializable {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 电力名称
     */
    private String name;

    /**
     * 排放因子
     */
    private BigDecimal factorValue;

    /**
     * 用量统计来源
     */
    private Integer countSource;

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
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}
