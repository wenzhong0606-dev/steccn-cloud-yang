package com.steccn.dp.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @desc 业务监控
 */
@Data
public class DpBusiMonitorDTO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Integer id;

    /**
     * 告警code
     */
    @NotNull(message = "告警不能为空")
    private String alarmCode;

    /**
     * 0-停用 1-启用
     */
    @NotNull(message = "停用启用不能为空")
    private Integer state;

    /**
     * 上限值
     */
    private BigDecimal upValue;

    /**
     * 下限值
     */
    private BigDecimal lowValue;

    /**
     * 监控范围
     */
    private List<Integer> monitorRange;

    /**
     * 通知人员
     */
    @NotEmpty(message = "告警接收人员不能为空")
    private Set<Integer> notifyMan;

}
