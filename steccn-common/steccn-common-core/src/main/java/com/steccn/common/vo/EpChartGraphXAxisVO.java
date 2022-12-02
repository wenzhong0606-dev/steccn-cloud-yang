package com.steccn.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("能源绩效图表x轴vo")
public class EpChartGraphXAxisVO implements Serializable {

    @ApiModelProperty("第一x轴")
    private List<String> data;

    @ApiModelProperty("第二x轴")
    private List<String> data2;

}
