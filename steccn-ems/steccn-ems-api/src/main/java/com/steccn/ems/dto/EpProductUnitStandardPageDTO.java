package com.steccn.ems.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * 产品单耗标准分页dto
 */
@Data
public class EpProductUnitStandardPageDTO extends BasePageDTO implements Serializable {
    /**
     * 能源类型id
     */
    private Integer energyType;

    /**
     * 标准类型
     */
    private Long standardType;

    /**
     * 产品类型
     */
    private Long productType;

    /**
     * 产品厚度
     */
    private Long productThick;

    /**
     * 标准名称
     */
    private String name;
}
