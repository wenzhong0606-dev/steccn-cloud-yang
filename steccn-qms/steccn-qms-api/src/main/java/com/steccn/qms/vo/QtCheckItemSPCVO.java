package com.steccn.qms.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * spc检验项下拉框的返回数据
 */
@Data
public class QtCheckItemSPCVO {
    /**
     * 检验项id
     */
    private Integer checkItemId;

    /**
     * 检验项名称
     */
    private String checkItemName;


}
