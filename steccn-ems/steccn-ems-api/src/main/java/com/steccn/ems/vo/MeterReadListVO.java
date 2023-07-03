package com.steccn.ems.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/12/1 9:00
 */
@Data
public class MeterReadListVO {
    private Integer meterId;
    private String meterName;
    private Long partitionNode;
    private String partitionNodeName;
    private Long parentId;
    private Integer sort;

    @ApiModelProperty("合计")
    private BigDecimal sum;

    /**
     * 是否绑定仪表 0否，1是
     */
    private Integer isBindingMeter;
    /**
     * 仪表状态
     */
    private Integer meterStatus;
    /**
     * 0实体远传仪表、1实体非远传仪表、2虚拟仪表
     */
    private Integer entityTable;
    private List<ReadValueListVO> readValueList;
    /**
     * 递归集合
     */
    private List<MeterReadListVO> meterReadListVOList;
}
