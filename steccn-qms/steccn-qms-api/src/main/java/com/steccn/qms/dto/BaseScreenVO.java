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

}
