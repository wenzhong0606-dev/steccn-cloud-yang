package com.steccn.qms.dto;

import com.steccn.common.vo.BaseSelectVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 筛选汇总
 */
@ApiModel("筛选")
@Data
public class BaseScreenVO implements Serializable {

    @ApiModelProperty("物料筛选")
    private List<BaseSelectVO<Long>> materialScreen;

    @ApiModelProperty("取样时间筛选")
    private List<BaseSelectVO<LocalDate>> sampleTimeScreen;

    @ApiModelProperty("产线筛选")
    private List<BaseSelectVO<Integer>> productLineScreen;

    @ApiModelProperty("车间筛选")
    private List<BaseSelectVO<Integer>> productWorkShopScreen;

    @ApiModelProperty("班次筛选")
    private List<BaseSelectVO<Integer>> workingOrderScreen;

    @ApiModelProperty("班组筛选")
    private List<BaseSelectVO<Integer>> classGroupScreen;

    @ApiModelProperty("缺陷类别筛选")
    private List<BaseSelectVO<Integer>> categoryScreen;

    @ApiModelProperty("缺陷项筛选")
    private List<BaseSelectVO<Integer>> defectItemScreen;

}
