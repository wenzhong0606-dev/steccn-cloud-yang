package com.steccn.pms.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 基片统计报表
 */
@Data
public class JpCountReportDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期格式
     */
    private List<String> dateStrList;

    /**
     * 行对象格式
     */
    private List<JpCountReportDetailRowVO> rowList;
}
