package com.steccn.qms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.validation.constraints.NotNull;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 设置目标值
 *
 * @author AndrewLuo
 * @date 2022/11/08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("部门分析驾驶舱模块DTO")
public class QtChatDepartmentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer key;

    private BigDecimal value;

    @NotNull
    private List<Integer> keys;

    @ApiModelProperty("筛选日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate selectDate;
}
