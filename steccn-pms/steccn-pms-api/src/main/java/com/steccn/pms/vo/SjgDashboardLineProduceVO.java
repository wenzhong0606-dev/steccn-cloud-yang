package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author yequan
 * @desc 深加工看板
 * @date 2023/9/5
 */
@Data
@ApiModel("深加工看板")
public class SjgDashboardLineProduceVO implements Serializable {

    @ApiModelProperty("表头")
    private List<String> headList;

    @ApiModelProperty("产量")
    private List<BigDecimal> pmList;

    @ApiModelProperty("入库")
    private List<BigDecimal> inStorageList;

    @ApiModelProperty("耗用")
    private List<BigDecimal> consumeList;
}
