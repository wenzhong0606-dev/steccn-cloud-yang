package com.steccn.ems.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chenhuan
 * @date 2022/12/7 17:20
 */
@Data
public class EpMeterOperationRecordVO {

    /**
     * 操作节点
     */
    private Integer opNode;

    /**
     * 操作人名称
     */
    private String opName;

    private String opNodeName;

    /**
     * 操作时间
     */
    private LocalDateTime opTime;
}
