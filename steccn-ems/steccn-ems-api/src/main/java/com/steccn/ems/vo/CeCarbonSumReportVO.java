package com.steccn.ems.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 碳排放总量集合vo
 */
@Data
public class CeCarbonSumReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 表头集合
     */
    private List<String> headList;

    /**
     * 内容集合
     */
    private List<CeCarbonSumReportListVO> contentList;
}
