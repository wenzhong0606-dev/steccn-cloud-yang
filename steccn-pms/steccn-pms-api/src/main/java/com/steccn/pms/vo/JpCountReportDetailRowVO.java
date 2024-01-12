package com.steccn.pms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author yequan
 * @desc 基片统计报表行对象
 */
@Data
public class JpCountReportDetailRowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 0 是普通行
     * 1 是小计行
     * 2 是 (落板破玻璃/拉引量/生产产量/入库成品率行)
     */
    private Integer type;

//    /**
//     * 排序，用于区分内容之间的次序关系
//     */
//    private Integer sort;

    /**
     * 分类
     */
    private String category;

    /**
     * 内容名称
     */
    private String name;

    /**
     * 单位名称
     */
    private String unitName;

    /**
     * 内容值
     */
    private List<BigDecimal> valueList;

    /**
     * 行合计
     */
    private BigDecimal rowCount;

}
