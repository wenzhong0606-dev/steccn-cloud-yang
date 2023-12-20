package com.steccn.platform.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 新增标准产能
 * @date 2023/12/20
 */
@Data
public class SysProductionCapacityAddDTO implements Serializable {

    /**
     * 日期
     */
    @NotNull(message = "日期不能为空")
    private LocalDate classDate;

    /**
     * 标准产能
     */
    @NotNull(message = "标准产能不能为空")
    @DecimalMin(value="0",message="数量应大于0")
    @DecimalMax(value="99999999.99",message="数量大小应小于99999999.99")
    private BigDecimal standardCapacity;

    /**
     * 产线基数
     */
    @NotNull(message = "产线基数不能为空")
    private Integer lineNum;

}

