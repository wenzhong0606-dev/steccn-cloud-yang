package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yequan
 * @desc 配料抽检日报表sql查询dto
 * @date 2023/4/19
 */
@Data
@ApiModel("来料检dto")
public class QtIncomingCheckDTO implements Serializable {

    @ApiModelProperty("任务id")
    private Long taskId;

    @ApiModelProperty("配料付数")
    private Integer ingredientNum;

    @ApiModelProperty("检验结果")
    private String checkResult;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("检验项")
    private Integer itemId;

    @ApiModelProperty("检验任务的检验结果")
    private String result;

    @ApiModelProperty("取样时间数组")
    private List<LocalDateTime> samplingTimeArray;

    @ApiModelProperty("取样时间")
    private LocalDateTime samplingTime;

    @ApiModelProperty("样本")
    private Integer copy;

    @ApiModelProperty("物料使用车间")
    private QtBaseIdNameDTO materialWorkshop;

    @ApiModelProperty("供应商")
    private QtBaseIdNameDTO supplier;
}
