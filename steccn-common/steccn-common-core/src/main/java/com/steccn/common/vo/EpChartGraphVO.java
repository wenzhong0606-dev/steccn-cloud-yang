package com.steccn.common.vo;

import com.steccn.common.dto.QtSeriesDataDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 能源绩效vo
 */

@Data
@ApiModel("能源绩效图vo")
public class EpChartGraphVO<T, Q> implements Serializable {

    /**
     * x轴数据
     */
    private T xData;

    /**
     * 图例
     */
    private List<String> legend;

    /**
     * 第二层数据
     */
    private List<QtSeriesDataDTO> seriesDataVO;

    /**
     * 标准值 企业标准 | 行业标准
     */
    private List<StandardVO> standard;

    /**
     * 单耗预警信息
     */
    private List<EpChartUnitConsumeAlarmVO> alarmList;

    /**
     * 其余冗余数据
     */
    private Q other;
}
