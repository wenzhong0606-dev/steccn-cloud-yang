package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 样本详情DTO
 *
 * @author AndrewLuo
 * @date 2022/10/11
 */
@Data
public class QtCheckTaskCopyDetailsDTO implements Serializable {
    /**
     * 样本编号
     */
    private String sampleNum;

    /**
     * 车牌号
     */
    private String licensePlate;

    /**
     * 分析人
     */
    private Integer analyst;

    /**
     * 分析人姓名
     */
    private String analystName;

    /**
     * 采样时间
     */
    private LocalDateTime samplingTime;

    /**
     * 分析时间
     */
    private LocalDateTime analystTime;

    /**
     * 配料付数
     */
    private Integer ingredientNum;

    /**
     * 结果
     */
    private Map<String, String> result;

    /**
     *
     */
    private Boolean sampleFlag;
}
