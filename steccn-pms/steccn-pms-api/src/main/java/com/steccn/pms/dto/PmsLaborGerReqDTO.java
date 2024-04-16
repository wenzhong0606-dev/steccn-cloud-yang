package com.steccn.pms.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/04/15
 */
@Data
public class PmsLaborGerReqDTO implements Serializable {
    @NotEmpty
    private List<PmsLaborGerReqDetailDTO> details;
}
