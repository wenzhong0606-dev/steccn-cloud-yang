package com.steccn.ems.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EpEnergyPriceDTO implements Serializable {
    /**
     * 价格方案id
     */
    private Long id;

    /**
     * 有效时间-开始
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDate validStart;

    /**
     * 有效时间-结束
     */
    @NotNull(message = "结束时间不能为空")
    private LocalDate validEnd;

    /**
     * 峰时段单价
     */
    @NotNull(message = "峰时段单价不能为空")
    @Max(value = 100)
    @Min(value = 0)
    private BigDecimal peakPrice;

    /**
     * 平时段单价
     */
    @NotNull(message = "平时段单价不能为空")
    @Max(value = 100)
    @Min(value = 0)
    private BigDecimal peacePrice;

    /**
     * 谷时段单价
     */
    @NotNull(message = "谷时段单价不能为空")
    @Max(value = 100)
    @Min(value = 0)
    private BigDecimal valleyPrice;

    /**
     * 单位
     */
    @NotNull
    private Long unit;

    /**
     * 能源价格方案详情
     */
    @NotEmpty(message = "单元格不能为空")
    private List<Integer> pricePlanUnit;

    private static final long serialVersionUID = 1L;
}
