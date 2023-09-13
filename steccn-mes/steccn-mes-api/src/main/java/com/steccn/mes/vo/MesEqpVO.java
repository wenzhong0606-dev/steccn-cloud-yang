package com.steccn.mes.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 设备VO
 *
 * @author AndrewLuo
 * @date 2023/09/01
 */
@Data
public class MesEqpVO implements Serializable {
    /**
     * 设备编码
     */
    private String equipmentCode;

    /**
     * 设备名称
     */
    private String equipmentName;
    /**
     * 备注信息
     */
    private String remarks;
    /**
     * 型号
     */
    private String model;
    /**
     * 设备部位
     */
    private List<MesEqpPartVO> parts;
}
