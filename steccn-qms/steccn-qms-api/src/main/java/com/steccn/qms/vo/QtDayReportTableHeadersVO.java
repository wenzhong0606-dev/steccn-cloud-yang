package com.steccn.qms.vo;

import com.steccn.pms.vo.BaseReportHeaderVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 日报表头VO
 *
 * @author AndrewLuo
 * @date 2023/01/06
 */
@Data
public class QtDayReportTableHeadersVO implements Serializable {
    @ApiModelProperty("表头-静态部分")
    private List<QtSpcHeadTitleVO> common;

    @ApiModelProperty("表头-动态部分")
    private List<BaseReportHeaderVO> module;
}
