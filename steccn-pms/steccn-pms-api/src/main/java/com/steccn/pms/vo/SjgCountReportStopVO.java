package com.steccn.pms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 深加工停机报表
 * @date 2023/12/27
 */
@Data
public class SjgCountReportStopVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String processName;

    private List<SjgCountReportStopRowVO> table;

}
