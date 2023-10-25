package com.steccn.mes.dto;

import lombok.Data;

/**
 * 缺陷聚合
 *
 * @author AndrewLuo
 * @date 2023/10/20
 */
@Data
public class MesDefectCountDTO {
    /**
     * 数量
     */
    private Long num;
    /**
     * 设备
     */
    private String eqpId;
    /**
     * 所属部门
     */
    private String officeName;
}
