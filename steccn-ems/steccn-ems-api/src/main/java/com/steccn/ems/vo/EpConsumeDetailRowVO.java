package com.steccn.ems.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @desc 能耗明细行数据视图对象
 * @author yequan
 */
@Data
@ApiModel("能耗明细行数据视图对象")
public class EpConsumeDetailRowVO implements Serializable {

    @ApiModelProperty("班次日期")
    private LocalDate classDate;

    @ApiModelProperty("班次日期字符串")
    private String classDateStr;

    @ApiModelProperty("班次id")
    private Integer classId;

    @ApiModelProperty("白班夜班字符串")
    private String classIdStr;

    @ApiModelProperty("厚度字符串")
    private String thickStr;

    @ApiModelProperty("能耗数据集合")
    private List<String> consumeList;

}
