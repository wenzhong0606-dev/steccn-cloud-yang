package com.steccn.mes.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 生产领料单DTO
 *
 * @author AndrewLuo
 * @date 2023/08/04
 */
@Data
public class ErpSppickMtrlSaveDTO extends ErpCommonSaveDTO<ErpSppickMtrlSaveDetailDTO> implements Serializable {
    /**
     * 生产车间编码
     */
    private String fdeptno;
}
