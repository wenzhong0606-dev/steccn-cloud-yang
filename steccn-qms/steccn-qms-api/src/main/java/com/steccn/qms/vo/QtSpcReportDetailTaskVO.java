package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
@Data
public class QtSpcReportDetailTaskVO implements Serializable {
    /**
     * 检验任务单号
     */
    private Integer checkTaskId;

    /**
     * 检验完成时间
     */
    private LocalDate finishedTime;

    /**
     * 检验结果
     */
    private Integer result;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 生产产线
     */
    private List<QtBaseIdNameDTO> productLine;

    /**
     * 客户
     */
    private List<QtBaseIdNameDTO> customer;
}
