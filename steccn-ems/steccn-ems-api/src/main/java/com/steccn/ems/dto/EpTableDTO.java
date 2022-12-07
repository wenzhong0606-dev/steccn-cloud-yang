package com.steccn.ems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 能耗图表dto
 */
@ApiModel("能耗图表dto")
@Data
public class EpTableDTO implements Serializable {

    @ApiModelProperty("能源类型id")
    @NotNull(message = "能源类型不能为空")
    private Integer energyType;

    @ApiModelProperty("开始日期")
    @NotNull(message = "开始日期不能为空")
    private LocalDate start;

    @ApiModelProperty("结束日期")
    @NotNull(message = "结束日期不能为空")
    private LocalDate end;

    @ApiModelProperty("厚度")
    private BigDecimal thick;

    @ApiModelProperty("时间-天、周、月、无粒度")
    @NotNull(message = "时间粒度不能为空")
    private Integer granularity;

    @ApiModelProperty("分区-组织机构id")
    @NotNull(message = "组织机构不能为空")
    @Size(min = 1, message = "组织机构不能为空")
    private List<Integer> officeIdList;

}
