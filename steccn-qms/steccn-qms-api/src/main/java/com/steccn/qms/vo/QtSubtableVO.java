package com.steccn.qms.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/11/17
 */
@Data
public class QtSubtableVO implements Serializable {

    /**
     * 名称
     */
    private String name;

    /**
     * 值
     */
    private String value;

    /**
     * 表头
     */

    private String label;

    /**
     * 任务id
     */
    @JsonIgnore
    private Long taskId;

    /**
     * 样本数
     */
    @JsonIgnore
    private Integer copy;

    /**
     * 样本序号
     */
    @JsonIgnore
    private String sampleNum;

    /**
     * 车牌号
     */
    @JsonIgnore
    private String licensePlate;
}
