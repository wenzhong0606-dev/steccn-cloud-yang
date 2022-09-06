package com.steccn.pms.dto;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.io.Serializable;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/5
 */
@Data
public class PmProcessStationDTO implements Serializable {
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("执行工位名称")
    private String executeStationName;

    @ApiModelProperty("执行工位id")
    private Integer executeStationId;

    @ApiModelProperty("工序id")
    private Integer processId;
}
