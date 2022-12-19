package com.steccn.ems.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @desc 能源明细vo
 * @author yequan
 */
@Data
@ApiModel("能源明细vo")
public class EpDetailVO implements Serializable {

    @ApiModelProperty("班次日期")
    private LocalDate classDate;

    @ApiModelProperty("班次日期字符串")
    private String classDateStr;

    @ApiModelProperty("班次id")
    private Integer classId;

    @ApiModelProperty("白班夜班字符串")
    private String classIdStr;

    @ApiModelProperty("厚度")
    private BigDecimal thick;

    @ApiModelProperty("厚度字符串")
    private String thickStr;

    @ApiModelProperty("组织机构的sort")
    private Integer sort;

}
