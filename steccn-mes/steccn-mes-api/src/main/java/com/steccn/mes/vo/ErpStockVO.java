package com.steccn.mes.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * erp仓库数据
 *
 * @author AndrewLuo
 * @date 2023/08/29
 */
@Data
public class ErpStockVO implements Serializable {
    private String fstockid;
    private String fnumber;
    private String fname;
}
