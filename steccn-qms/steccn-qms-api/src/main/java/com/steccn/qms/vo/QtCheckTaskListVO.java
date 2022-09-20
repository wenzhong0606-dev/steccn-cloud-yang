package com.steccn.qms.vo;

import lombok.Data;

/**
 * 巡检任务列表vo
 *
 * @author AndrewLuo
 * @date 2022/09/09
 */
@Data
public class QtCheckTaskListVO {
    /**
     * 任务id
     */
    private Long id;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 车间名称
     */
    private String workshopName;

    /**
     * 产线名称
     */
    private String productLineName;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 检验数量
     */
    private Integer checkNum;

    /**
     * 合格数量
     */
    private Integer goodNum;

    /**
     * 不合格数量
     */
    private Integer badNum;

    /**
     * 结果判定
     */
    private String result;

    /**
     * 备注
     */
    private String note;
}
