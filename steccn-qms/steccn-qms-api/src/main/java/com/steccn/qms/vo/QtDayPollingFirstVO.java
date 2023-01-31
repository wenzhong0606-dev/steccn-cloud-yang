package com.steccn.qms.vo;

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
public class QtDayPollingFirstVO implements Serializable {

    /**
     * 缺陷表头
     */
    private QtDayReportTableHeadersVO flawHeaders;

    /**
     * 缺陷数据
     */
    private List<QtDayReportPollingDataVO> flawTableData;
}
