package com.steccn.wms.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 销售系统事务同步DTO
 *
 * @author AndrewLuo
 * @date 2023/09/25
 */
@Data
public class SalesTransactionHeaderDTO implements Serializable {
    private String applicationGroupCode;
    private String externalSystemCode;
    private String batchNum;
    private String interfaceCode;
    private String userName;
    private String applicationCode;
    private String batchCount;
}
