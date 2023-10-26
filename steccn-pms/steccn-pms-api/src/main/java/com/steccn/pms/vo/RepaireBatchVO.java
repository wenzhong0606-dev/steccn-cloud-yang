package com.steccn.pms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 返洗不良报表
 * @date 2023/10/26
 */
@Data
public class RepaireBatchVO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 数据行集合
     */
    private List<RepaireBatchListVO> dataList;

    /**
     * 表头-缺陷
     */
    private List<BaseReportHeaderVO> headerList;


}
