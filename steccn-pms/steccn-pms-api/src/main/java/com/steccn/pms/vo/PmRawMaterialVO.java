package com.steccn.pms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 *
 * @author AndrewLuo
 * @date 2023/12/19
 */
@Data
public class PmRawMaterialVO implements Serializable {
    /**
     * 电子称编码
     */
    private String tagId;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 值
     */
    private BigDecimal value;

    /**
     * 日期
     */
    private LocalDate classDate;
}
