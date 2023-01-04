package com.steccn.qms.vo;

import com.steccn.pms.vo.BaseReportHeaderVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 质量日报表-首检视图对象
 *
 * @author AndrewLuo
 * @date 2023/01/04
 */
@Data
public class QtDayReportFirstVO implements Serializable {
    /**
     * 表头-静态部分
     */
    private List<QtSpcHeadTitleVO> common;

    /**
     * 表头-动态部分
     */
    private List<BaseReportHeaderVO> module;
}
