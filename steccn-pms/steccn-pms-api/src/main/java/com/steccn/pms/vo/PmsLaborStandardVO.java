package com.steccn.pms.vo;

import com.steccn.pms.dto.PmsLaborStandardDetailDTO;
import lombok.Data;

import java.util.List;

/**
 * 劳保领用标准VO
 *
 * @author AndrewLuo
 * @date 2024/04/03
 */
@Data
public class PmsLaborStandardVO {
    private Integer id;
    private List<Integer> officeId;
    private String riskFactor;
    private List<PmsLaborStandardDetailVO> details;
}
