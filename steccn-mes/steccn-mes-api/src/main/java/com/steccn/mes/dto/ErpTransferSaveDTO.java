package com.steccn.mes.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 调拨单保存DTO
 *
 * @author AndrewLuo
 * @date 2023/07/27
 */
@Data
public class ErpTransferSaveDTO implements Serializable {
    /**
     * mes单据内码
     */
    private String fmesbillid;
    /**
     * mes单据编号
     */
    private String fmesbillno;
    /**
     * 单据日期
     */
    private LocalDate fdate;

    /**
     * 分录明细实体
     */
    List<ErpTransferSaveDetailDTO> fentry;
}
