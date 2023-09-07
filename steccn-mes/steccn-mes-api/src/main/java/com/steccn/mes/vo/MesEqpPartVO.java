package com.steccn.mes.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 获取Mes设备部位
 *
 * @author AndrewLuo
 * @date 2023/09/01
 */
@Data
public class MesEqpPartVO implements Serializable {
    /**
     * 部位
     */
    private String part;
    /**
     * 维修方式
     */
    private String maintainMethod;
}
