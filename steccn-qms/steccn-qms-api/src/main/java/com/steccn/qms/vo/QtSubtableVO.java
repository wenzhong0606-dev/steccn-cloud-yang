package com.steccn.qms.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.models.auth.In;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
     * 样本时间
     */
    private LocalDateTime samplingTime;

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

    /**
     * 配料附数
     */
    @JsonIgnore
    private Integer ingredientNum;
}
