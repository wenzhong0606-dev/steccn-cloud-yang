package com.steccn.qms.vo;

import lombok.Data;

import java.util.List;

@Data
public class QtIQCAcceptabilityCopyVO {
    /**
     * 样本序号
     */
    private String sampleNum;

    /**
     * 车牌号
     */
    private String licensePlate;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 动态子表格内容
     */
    private List<QtSubtableVO> list;
}
