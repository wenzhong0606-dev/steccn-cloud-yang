package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 */
@Data
@ApiModel("能耗统计dto")
public class EnergyCountDTO implements Serializable {

    private Integer date;


}
