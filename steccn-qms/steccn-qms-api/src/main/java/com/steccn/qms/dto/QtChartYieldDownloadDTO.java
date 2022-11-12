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
     * 产线名字
     */
    private String workLineName;

    /**
     * 班次名字
     */
    private String className;

    /**
     * 对比维度
     */
    private String compare;

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

    private String base64;
}
