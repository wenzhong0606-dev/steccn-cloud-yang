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
}
