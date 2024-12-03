package com.steccn.pms.dto;

import lombok.Data;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/04/15
 */
@Data
public class PmsLaborGerReqDetailDTO implements Serializable {
    private String loginCode;
    private Long materialId;
    private String depCode;
    private BigDecimal num;
    private File file;
    private String type;
    private String postName;
}
