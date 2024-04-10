package com.steccn.pms.dto;

import com.steccn.common.dto.ValidList;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 劳保标准DTO
 *
 * @author AndrewLuo
 * @date 2024/04/03
 */
@Data
public class PmsLaborStandardDTO {
    private Integer id;
    @NotEmpty(message = "机构不能为空")
    private List<String> officeId;
    private String riskFactor;
    @Valid
    private List<PmsLaborStandardDetailDTO> details;
    private List<Integer> ids;
}
