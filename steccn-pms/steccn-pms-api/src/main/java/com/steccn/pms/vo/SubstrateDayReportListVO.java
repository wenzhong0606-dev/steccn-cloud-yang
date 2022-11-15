package com.steccn.pms.vo;

import com.steccn.common.vo.BaseQuantityVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 基片日报
 */
@Data
@ApiModel("基片日报表vo")
public class SubstrateDayReportListVO implements Serializable {

    @ApiModelProperty("类型 0-数据 1-产线合计 2-车间合计 3-总合计")
    private Integer type;

    @ApiModelProperty("车间名称")
    private String workshopName;

    @ApiModelProperty("产线名称")
    private String productLineName;

    @ApiModelProperty("规格")
    private List<String> specList;

    @ApiModelProperty("早班")
    private List<String> morning;

    @ApiModelProperty("夜班")
    private List<String> night;

    @ApiModelProperty("总计")
    private List<String> total;

}
