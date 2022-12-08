package com.steccn.common.dto;

import com.steccn.common.vo.StandardVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @desc 能源绩效图表dto
 */
@ApiModel("能源绩效图表dto")
@Data
public class EpChartGraphDTO<E1, E2, E3> implements Serializable {

    @ApiModelProperty("数据集合")
    private List<EpChartGraphxAxisDTO<E1, E2, E3>> dataList;

    @ApiModelProperty("标准-企业，行业")
    private List<StandardVO> standardList;

    @ApiModelProperty("能源类型")
    private Integer energyType;

    @ApiModelProperty("组织机构map")
    private Map<E1, String> officeMap;

    @ApiModelProperty("部门标准map")
    private Map<E1, E3> standardMap;

    @ApiModelProperty("条件时间")
    private LocalDate conditionDate;

}
