package com.steccn.pms.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/3 14:15
 */
@Data
public class PmsProducePlanDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<PmsProducePlanDetailsDTO> producePlanDetailsDTOList;
    private String materialCode;
    private Long materialId;
    private String materialName;
    private BigDecimal total;
}
