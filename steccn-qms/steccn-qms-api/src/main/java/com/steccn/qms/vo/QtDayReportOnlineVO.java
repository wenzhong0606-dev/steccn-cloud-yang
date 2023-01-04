package com.steccn.qms.vo;

import com.steccn.pms.vo.BaseReportHeaderVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 质量日报表-在线检视图对象
 */
@Data
@ApiModel("质量日报表-在线检视图对象")
public class QtDayReportOnlineVO implements Serializable {

    @ApiModelProperty("表头-静态部分")
    private List<QtSpcHeadTitleVO> common;

    @ApiModelProperty("表头-动态部分")
    private List<BaseReportHeaderVO> module;

    @ApiModelProperty("数据部分")
    private List<QtDayReportOnlineDataVO> tableData;

}
