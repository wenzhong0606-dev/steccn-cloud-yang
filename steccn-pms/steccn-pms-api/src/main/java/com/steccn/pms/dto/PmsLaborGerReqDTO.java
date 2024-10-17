package com.steccn.pms.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/04/15
 */
@Data
public class PmsLaborGerReqDTO implements Serializable {
    private List<Map<String,String>>  details;
    private Long typeId;
    private Long fileId;
//    private List<PmsLaborGerReqDetailDTO> details;
}
