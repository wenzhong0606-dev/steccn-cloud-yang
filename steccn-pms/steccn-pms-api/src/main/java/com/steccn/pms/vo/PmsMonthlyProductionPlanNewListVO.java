package com.steccn.pms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PmsMonthlyProductionPlanNewListVO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 每行数据
     */
    private List<PmsMonthlyProductionPlanNewRowVO> dataList;

    /**
     * 动态列
     */
    private List<String> dateList;


}
