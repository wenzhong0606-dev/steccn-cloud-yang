package com.steccn.data.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 设备运行记录
 *
 * @author AndrewLuo
 * @date 2023/09/05
 */
@Data
public class OperationRecordVO implements Serializable {
    private String code;
    private String eqpName;
    private Integer value;
}
