package com.steccn.ems.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;
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
    private Long productThickness;

    /**
     * 标准名称
     */
    private String standardName;
}
