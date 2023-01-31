package com.steccn.qms.vo;

import com.steccn.pms.vo.BaseReportHeaderVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 质量日报表-首检/巡检视图对象
 *
 * @author AndrewLuo
 * @date 2023/01/04
 */
@Data
public class QtDayReportFirstVO implements Serializable {
    /**
     * 表头部分
     */
    private QtDayReportTableHeadersVO headers;

    /**
     * 数据部分
     */
    private List<QtDayReportFirstDataVO> tableData;

    /**
     * 落班时段
     */
    private List<QtCheckTaskDestoryOnlineVO> fallList;


    /**
     * 缺陷表头
     */
    private QtDayReportTableHeadersVO flawHeaders;

    /**
     * 缺陷数据
     */
    private List<QtDayReportOnlineDataVO> flawTableData;
}
