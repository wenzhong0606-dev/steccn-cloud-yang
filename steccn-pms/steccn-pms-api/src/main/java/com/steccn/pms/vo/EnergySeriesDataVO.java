package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 */
@Data
@ApiModel("能耗vo")
public class EnergySeriesDataVO implements Serializable {

    @ApiModelProperty("电")
    private List<EnergyCountListVO> electric;

    @ApiModelProperty("水")
    private List<EnergyCountListVO> water;

    @ApiModelProperty("油")
    private List<EnergyCountListVO> oil;

    @ApiModelProperty("气")
    private List<EnergyCountListVO> gas;

}
