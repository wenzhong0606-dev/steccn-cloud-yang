package com.steccn.wms.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 销售事务同步请求体
 *
 * @author AndrewLuo
 * @date 2023/09/25
 */
@Data
public class SalesTransactionBodyDTO implements Serializable {
    /**
     * 外部事务编号
     */
    private String esTrxNum;
    /**
     * 事务年度
     */
    private Long trxYear;
    /**
     * 外部系统所属业务实体代码
     */
    private String esOuCode;
    /**
     * 事务日期
     */
    private String trxDate;
    /**
     * 数据来源
     */
    private String sourceCode;

    private List<SalesTransactionBodyDetailDTO> rcvTrxLineList;
}
