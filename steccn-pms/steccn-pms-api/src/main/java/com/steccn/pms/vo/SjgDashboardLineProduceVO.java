package com.steccn.pms.vo;

import com.steccn.common.dto.SteccnRow;
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

    @ApiModelProperty("数据行对象")
    private List<SteccnRow<String, List<BigDecimal>>> rowList;
}
