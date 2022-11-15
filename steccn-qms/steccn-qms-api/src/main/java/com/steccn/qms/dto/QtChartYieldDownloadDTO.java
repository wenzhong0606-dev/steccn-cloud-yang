package com.steccn.qms.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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
     * 对比 0为班次 1为产线 2为车间
     */
    private Integer compare;

    /**
     * 产线id
     */
    private Integer productLineId;

    /**
     * 产线名字
     */
    private String productLineName;

    /**
     * 车间id
     */
    private Integer productWorkShopId;

    /**
     * 车间名字
     */
    private String productWorkShopName;

    /**
     * 班次id 0为早班 1为晚班 3为全部
     */
    private Integer workOrderId;

    /**
     * 班次名字
     */
    private String workOrderName;

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

    /**
     * 过滤条件
     */
    private Set<String> filters;
}
