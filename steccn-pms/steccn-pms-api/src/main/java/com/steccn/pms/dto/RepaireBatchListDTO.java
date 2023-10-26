package com.steccn.pms.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 返洗不良日报表
 * @date 2023/10/16
 */
@Data
public class RepaireBatchListDTO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * * 产线集合
     */
    private List<Integer> productLineList;

    /**
     * * 开始日期
     */
    private LocalDate start;

    /**
     * * 结束日期
     */
    private LocalDate end;
}
