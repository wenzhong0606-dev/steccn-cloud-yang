package com.steccn.qms.vo;

import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;
import java.util.List;

@Data
public class QtSpcReportDetailTaskAllVO<T> implements Serializable {
    /**
     * 表头
     */
    private List<String> header;

    /**
     * 任务详情
     */
    private List<T> reportDetailTaskVOList;
}
