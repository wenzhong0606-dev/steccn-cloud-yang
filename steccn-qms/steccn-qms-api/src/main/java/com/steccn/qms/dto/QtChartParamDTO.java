package com.steccn.qms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 图表统一入参
 *
 * @author AndrewLuo
 * @date 2022/11/10
 */
@Data
public class QtChartParamDTO extends BasePageDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 开始时间
     */
    @NotNull(message = "时间范围不能为空")
    private LocalDate dateStart;

    /**
     * 结束时间
     */
    @NotNull(message = "时间范围不能为空")
    private LocalDate dateEnd;

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
    private Integer productWorkShopName;

    /**
     * 班次id 0为早班 1为晚班 3为全部
     */
    private Integer workOrderId;

    /**
     * 类型 0为柱状图 1为饼状图
     */
    private Integer type;

    private String productWorkName;

    @ApiModelProperty("检验项id")
    private Integer taskId;
}
