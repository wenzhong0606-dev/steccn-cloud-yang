package com.steccn.qms.vo;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QtIQCAcceptabilityCopyVO {
    /**
     * 样本编号
     */
    private String sampleNum;

    /**
     * 样本序号
     */
    private Integer copy;

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

    /**
     * 配料附数
     */
    private Integer ingredientNum;

    /**
     * 取样时间
     */
    private LocalDateTime samplingTime;

    @ApiModelProperty("元素")
    private List<String> eleList;
}
