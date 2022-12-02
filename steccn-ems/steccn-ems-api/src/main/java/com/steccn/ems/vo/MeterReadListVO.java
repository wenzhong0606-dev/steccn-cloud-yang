package com.steccn.ems.vo;

import lombok.Data;

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
