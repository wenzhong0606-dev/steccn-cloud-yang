package com.steccn.ems.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 * @desc 折标系数分页请求dto
 */
@Data
public class EpDiscountFactorPageDTO extends BasePageDTO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 系数类型
     */
    private Long type;

    /**
     * 能源类型
     */
    private Integer energyType;

    /**
     * 系数名称
     */
    private String name;

}
