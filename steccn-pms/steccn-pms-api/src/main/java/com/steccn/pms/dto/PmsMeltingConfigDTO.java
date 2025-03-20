package com.steccn.pms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/03/20
 */
@Data
public class PmsMeltingConfigDTO {
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "日期不能为空")
    @ApiModelProperty("日期")
    private LocalDate meltingDate;

    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "数量不能为空")
    @ApiModelProperty("数量")
    @DecimalMax(value="99999.999",message="数量大小应小于99999.999")
    @Digits(integer = 1,fraction = 4,message = "整数位上限为1位，小数位上限位4位")
    private BigDecimal meltingNum;
}
