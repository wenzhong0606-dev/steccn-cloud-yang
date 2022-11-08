package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 */
@ApiModel("报表表头")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseReportHeaderVO implements Serializable {

    @ApiModelProperty("标签")
    private String label;

    @ApiModelProperty("值")
    private List<String> value;
}
