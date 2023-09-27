package com.steccn.wms.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 销售系统事务同步DTO
 *
 * @author AndrewLuo
 * @date 2023/09/25
 */
@Data
public class SalesTransactionDTO implements Serializable {
    private SalesTransactionHeaderDTO header;
    private List<SalesTransactionBodyDTO> body;
}
