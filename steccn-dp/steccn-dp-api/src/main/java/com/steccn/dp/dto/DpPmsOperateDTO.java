package com.steccn.dp.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc
 * @date 2023/11/29
 */
@Data
public class DpPmsOperateDTO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 基片制造部，深加工制造部的组织机构主键
     */
    private Integer officeId;

    /**
     * 粒度
     */
    private Integer granularity;

    /**
     * 开始日期
     */
    private LocalDate start;

    /**
     * 结束日期
     */
    private LocalDate end;

}
