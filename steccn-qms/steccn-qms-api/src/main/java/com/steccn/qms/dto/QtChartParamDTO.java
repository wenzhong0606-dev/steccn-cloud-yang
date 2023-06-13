package com.steccn.qms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 图表统一入参
 *
 * @author AndrewLuo
 * @date 2022/11/10
 */
@Data
public class QtChartParamDTO extends BasePageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("开始时间")
    @NotNull(message = "时间范围不能为空")
    private LocalDate dateStart;

    @NotNull(message = "时间范围不能为空")
    @ApiModelProperty("结束时间")
    private LocalDate dateEnd;

    @ApiModelProperty("对比 0为班次 1为产线 2为车间 8为班组")
    private Integer compare;

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("产线名字")
    private String productLineName;

    @ApiModelProperty("产线id")
    private List<Integer> productLineIdList;

    @ApiModelProperty("车间id")
    private Integer productWorkShopId;

    @ApiModelProperty("车间名字")
    private Integer productWorkShopName;

    @ApiModelProperty("班次id 0为早班 1为晚班 3为全部")
    private Integer workingOrder;

    @ApiModelProperty("类型 0为柱状图 1为饼状图")
    private Integer type;

    @ApiModelProperty("生产车间名称")
    private String productWorkName;

    @ApiModelProperty("检验项id")
    private Integer taskId;

    @ApiModelProperty("缺陷项id")
    private Integer defectId;

    @ApiModelProperty("缺陷项类别id")
    private Integer defectCategory;

    @ApiModelProperty("缺陷项类型")
    private String defectType;

    @ApiModelProperty("检验方案类型编码:来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7")
    private Integer checkType;

    @ApiModelProperty("粒度:小时、天、周、月、季度、年 见QtSPCGranularityEnum枚举")
    private String granularity;

    @ApiModelProperty(value = "图表的过滤条件-用于筛选")
    private List<String> filters;

    @ApiModelProperty("折线图存在日期粒度，不对比模式下的图例")
    private List<String> guaFilters;

    private String base64;

    @ApiModelProperty(value = "班组名称")
    private String classGroupName;

    @ApiModelProperty(value = "班组id")
    private Integer classGroupId;
}
