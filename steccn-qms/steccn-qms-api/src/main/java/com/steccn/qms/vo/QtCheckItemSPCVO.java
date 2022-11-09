package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * spc检验项下拉框的返回数据
 */
@Data
public class QtCheckItemSPCVO implements Serializable {
    /**
     * 检验项id
     */
    private Integer checkItemId;

    /**
     * 检验项名称
     */
    private String checkItemName;


}
