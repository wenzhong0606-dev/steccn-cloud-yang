package com.steccn.qms.vo;

import com.steccn.pms.vo.BaseReportHeaderVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 不良日报表
 * @date 2023/7/21
 */
@Data
@ApiModel("不良日报表")
public class DefectDayVO implements Serializable {

    @ApiModelProperty("表头")
    private List<BaseReportHeaderVO> headerList;

    @ApiModelProperty("数据行")
    private List<DefectDayRowVO> rowList;

}
