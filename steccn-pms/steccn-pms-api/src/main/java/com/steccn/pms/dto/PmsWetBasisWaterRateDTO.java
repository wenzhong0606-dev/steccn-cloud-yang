package com.steccn.pms.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新湿基含水率DTO
 *
 * @author AndrewLuo
 * @date 2023/02/20
 */
@Data
public class PmsWetBasisWaterRateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 版本号
     */
    private String version;

    /**
     * 生料list
     */
    private List<PmsRecipeRawDTO> rawList;

    /**
     * 备注
     */
    private String note;
}
