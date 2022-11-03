package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 * @desc 基片日报
 */
@Data
@ApiModel("基片日报表vo")
public class SubstrateDayReportVO implements Serializable {

    private Integer type ;

    private Integer workshopId;

    private String workshopName;

    private Integer productLineId;

    private String productLineName;

    private MaterialSpecVO materialSpecVO;

//    private List<>


}
