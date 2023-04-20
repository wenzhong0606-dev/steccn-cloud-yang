package com.steccn.qms.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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
@ApiModel("配料抽检日报表sql查询dto")
public class QtDaySamplingDTO implements Serializable {

    @ApiModelProperty("任务id")
    private Long taskId;

    @ApiModelProperty("取样时间")
    private List<LocalDateTime> samplingTimeArray;

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

    @ApiModelProperty("取样时间")
    private LocalDateTime samplingTime;

}
