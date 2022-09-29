package com.steccn.pms.vo;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 工艺路线工序VO
 *
 * @author Mongknife
 * @date 2022/9/7
 */
@Data
@ApiModel("工艺路线工序VO")
public class PmCraftRouteProcessVO implements Serializable {

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("工序名称")
    private String processName;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("数量单位")
    private String unit;

    @ApiModelProperty("标准数量")
    private BigDecimal standardQuantity;

    @ApiModelProperty("标准数量单位")
    private String standardUnit;

    @ApiModelProperty("是否报工")
    private Integer reportWorkFlag;

    @ApiModelProperty("是否报检")
    private Integer reportCheckFlag;


    @ApiModelProperty("是否进站")
    private Integer inStationFlag;


    @ApiModelProperty("是否出站")
    private Integer outStationFlag;


    @ApiModelProperty("工序报废百分比")
    private BigDecimal processScrap;


}
