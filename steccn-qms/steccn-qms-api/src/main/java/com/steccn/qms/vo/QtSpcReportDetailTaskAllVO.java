package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class QtSpcReportDetailTaskAllVO implements Serializable {
    /**
     * 表头
     */
    private List<String> header;

    /**
     * 任务详情
     */
    private List<QtSpcReportDetailTaskVO> reportDetailTaskVOList;
}
