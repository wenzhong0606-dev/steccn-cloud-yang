package com.steccn.ems.vo;

import com.steccn.common.vo.BaseSelectVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EpProductUnitConsumeStandardVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 标准编码
     */
    private String code;

    /**
     * 标准名称
     */
    private String name;

    /**
     * 能源类型id
     */
    private Integer energyType;

    /**
     * 能源类型名称
     */
    private String energyTypeName;

    /**
     * 标准类型【企业标准、行业标准、部门标准】
     */
    private Long standardType;

    /**
     * 标准类型【企业标准、行业标准、部门标准】名称
     */
    private String standardTypeName;

    /**
     * 应用部门
     */
    private BaseSelectVO<Integer> applyOffice;

    /**
     * 产品类型【基片、深加工】
     */
    private Long productType;

    /**
     * 产品类型【基片、深加工】名称
     */
    private String productTypeName;

    /**
     * 产品厚度
     */
    private Long productThick;

    /**
     * 产品厚度数据字典名称
     */
    private String productThickName;

    /**
     * 单耗标准值
     */
    private BigDecimal unitValue;

    /**
     * 计量单位
     */
    private Long measureUnit;

    /**
     * 计量单位名称
     */
    private String measureUnitName;

    /**
     * 有效期-开始
     */
    private LocalDate validStart;

    /**
     * 有效期-结束
     */
    private LocalDate validEnd;

    /**
     * 有效期
     */
    private String validity;

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
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新人名称
     */
    private String updateName;

    /**
     * 更新人时间
     */
    private LocalDateTime updateTime;

}
