package com.steccn.qms.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 基片成品率下载DTO
 *
 * @author AndrewLuo
 * @date 2022/11/13
 */
@Data
public class QtChartYieldDownloadDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 车间名字
     */
    private String workShopName;

    /**
     * 车间id
     */
    private Integer workShopId;

    /**
     * 产线名字
     */
    private String workLineName;

    /**
     * 产线id
     */
    private Integer workLineId;

    /**
     * 班次名字
     */
    private String className;

    /**
     * 班次id
     */
    private Integer classId;

    /**
     * 对比维度
     */
    private Integer compare;

    /**
     * 图表的filter
     */
    private List<String> filters;

    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDate startTime;

    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    private LocalDate endTime;

    /**
     * 图表base64
     */
    private String base64;
}
