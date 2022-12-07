package com.steccn.ems.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 能耗明细视图对象
 */
@Data
@ApiModel("能耗明细视图对象")
public class EpConsumeDetailVO implements Serializable {

    @ApiModelProperty("数据集合")
    private List<EpConsumeDetailRowVO> dataList;

    @ApiModelProperty("表头数据")
    private List<String> headData;

}
