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

    @ApiModelProperty("类型")
    private Integer type;

    @ApiModelProperty("车间id")
    private Integer workshopId;

    @ApiModelProperty("车间名称")
    private String workshopName;

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("产线名称")
    private String productLineName;

    @ApiModelProperty("规格")
    private List<BaseQuantityVO> specList;

    @ApiModelProperty("早班，晚班，合计")
    private List<ProduceQuantityVO> classList;

}
