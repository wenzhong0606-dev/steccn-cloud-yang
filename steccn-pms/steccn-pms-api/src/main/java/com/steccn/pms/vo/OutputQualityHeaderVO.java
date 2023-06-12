package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.License;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("产量质量报表表头")
public class OutputQualityHeaderVO {

    @ApiModelProperty("标签")
    private String label;

    @ApiModelProperty("数据标签")
    private String prop;

    @ApiModelProperty("子标签")
    private List<OutputQualityHeaderVO> children;
}
