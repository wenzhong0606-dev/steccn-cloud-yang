package com.steccn.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * 生产订单甘特图VO
 *
 * @author AndrewLuo
 * @date 2023/11/02
 */
@Data
public class PmsProduceOrderGanttChartVO {
    private List<PmsProduceOrderGanttRowsVO> rows;
    private List<PmsProduceOrderGanttInfoVO> infos;
}
