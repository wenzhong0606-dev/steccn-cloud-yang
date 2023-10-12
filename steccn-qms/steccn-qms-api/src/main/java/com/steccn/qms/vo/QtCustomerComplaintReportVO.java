package com.steccn.qms.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 客诉报表的返回
 * @date 2023/10/12
 */
@Data
public class QtCustomerComplaintReportVO implements Serializable {

    /**
     * 客诉code
     */
    private String code;

    /**
     * 客诉主键
     */
    private Long id;

    /**
     * 投诉客户
     */
    private String complainCustomerName;

    /**
     * 回复客户日期
     */
    private LocalDate replyClientDate;

    /**
     * 物料主键
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 规格
     */
    private String specifications;

    /**
     * 投诉类型
     */
    private Integer complainType;

    /**
     * 投诉类型
     */
    private String complainTypeName;

    /**
     * 投诉数量
     */
    private BigDecimal complainQuantity;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 组件数量
     */
    private Integer moduleNum;

    /**
     * 组件序列号
     */
    private String moduleNumber;

    /**
     * 组件投诉原因
     */
    private String complaintReason;
}
