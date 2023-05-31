package com.steccn.ems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * @author yequan
 * @desc  【能耗全量数据表】样例
 * @date 2023/5/22
 */
@Data
@ApiModel("能耗全量数据表")
@AllArgsConstructor
@NoArgsConstructor
public class EpEnergyConsumeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("抄表日期")
    private LocalDate readDate;

    @ApiModelProperty("班次id 0早班 1晚班")
    private Integer classId;

    @ApiModelProperty("厚度")
    private BigDecimal thick;

    @ApiModelProperty("能耗map")
    private Map<Long, BigDecimal> consumeMap;

    @ApiModelProperty("产量map")
    private Map<Long, BigDecimal> yieldMap;

    public EpEnergyConsumeDTO(LocalDate readDate, Integer classId, BigDecimal thick, Map<Long, BigDecimal> consumeMap) {
        this.readDate = readDate;
        this.classId = classId;
        this.thick = thick;
        this.consumeMap = consumeMap;
    }

}
