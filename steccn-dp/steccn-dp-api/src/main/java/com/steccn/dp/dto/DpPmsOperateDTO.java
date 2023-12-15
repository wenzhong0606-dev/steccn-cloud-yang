package com.steccn.dp.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
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
    @NotNull(message = "组织机构不能为空")
    private Integer officeId;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate start;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    private LocalDate end;

    /**
     * 粒度
     */
    @NotNull(message = "粒度不能为空")
    private Integer granularity;

    /**
     * 马上删除 
     */
    private Integer type;

}
