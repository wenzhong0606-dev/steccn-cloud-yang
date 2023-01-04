package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 首检日报表-颜色-数值VO
 *
 * @author AndrewLuo
 * @date 2023/01/04
 */
@Data
public class ColorValueVO implements Serializable {
    /**
     * 0正常 1红色
     */
    private Integer color;

    /**
     * 数值
     */
    private String value;
}
