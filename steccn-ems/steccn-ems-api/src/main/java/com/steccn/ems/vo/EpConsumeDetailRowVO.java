package com.steccn.ems.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 能耗明细行数据视图对象
 */
@Data
@ApiModel("能耗明细行数据视图对象")
public class EpConsumeDetailRowVO extends EpDetailVO implements Serializable {

    @ApiModelProperty("能耗数据集合")
    private List<String> consumeList;

}
