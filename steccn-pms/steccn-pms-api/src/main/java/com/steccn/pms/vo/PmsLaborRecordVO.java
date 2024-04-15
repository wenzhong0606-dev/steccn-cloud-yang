package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 劳保用品领用记录VO
 *
 * @author AndrewLuo
 * @date 2024/04/15
 */
@Data
public class PmsLaborRecordVO {
    /**
     * 部门名称
     */
    private String depName;

    /**
     * 岗位名称
     */
    private String postName;

    /**
     * 人员
     */
    private String loginName;

    /**
     * 入职时间
     */
    private LocalDate startTime;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 领用时间
     */
    private LocalDateTime receiptTime;

    /**
     * 领用数量
     */
    private BigDecimal receiptNum;

    /**
     * 领用仓库
     */
    private String receiptWarehouseName;


    /**
     * 领用人姓名
     */
    private String receiptPersonName;

    /**
     * 领用单编码
     */
    private Integer receiptCode;
}
