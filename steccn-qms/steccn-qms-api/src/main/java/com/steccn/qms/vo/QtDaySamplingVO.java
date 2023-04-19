package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 配料抽检日报表
 * @date 2023/4/19
 */
@Data
@ApiModel("配料抽检日报表")
public class QtDaySamplingVO implements Serializable {

    @ApiModelProperty("动态表头")
    private List<String> dynamicHeadList;

    @ApiModelProperty("数据集合")
    private List<QtDaySamplingDataVO> dataList;
}
