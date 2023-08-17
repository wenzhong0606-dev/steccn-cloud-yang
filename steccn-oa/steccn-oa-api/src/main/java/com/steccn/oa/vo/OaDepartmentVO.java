package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * oa部门VO
 *
 * @author AndrewLuo
 * @date 2023/05/30
 */
@Data
public class OaDepartmentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 数据id
     */
    private Integer id;

    /**
     * 父级id
     */
    private Integer superId;

    /**
     * 部门名字
     */
    private String departmentName;

    /**
     * 树层级
     */
    private Integer tLevel;

    /**
     * 显示层级
     */
    private BigDecimal showOrder;

    /**
     *  树状层级
     */
    private Integer showOrderOfTree;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}
