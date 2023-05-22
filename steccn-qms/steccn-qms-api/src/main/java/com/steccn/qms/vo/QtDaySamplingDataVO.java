package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

/**
 * @author yequan
 * @desc 配料抽检日报表
 * @date 2023/4/19
 */
@Data
@ApiModel("配料抽检日报表")
public class QtDaySamplingDataVO implements Serializable {

    @ApiModelProperty("班次id 0早班 1晚班")
    private Integer classId;

    @ApiModelProperty("班次名称 早班 晚班")
    private String className;

    @ApiModelProperty("检验任务详情")
    private Long taskId;

    @ApiModelProperty("取样日时间")
    private String samplingTime;

    @ApiModelProperty("配料附数")
    private Integer ingredientNum;

    @ApiModelProperty("检验结果")
    private String result;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("检验项数据")
    private List<String> itemList;

}
