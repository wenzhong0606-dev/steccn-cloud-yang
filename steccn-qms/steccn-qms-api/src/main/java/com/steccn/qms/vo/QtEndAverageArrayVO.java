package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 截尾平均值VO
 *
 * @author AndrewLuo
 * @date 2022/09/28
 */
@Data
public class QtEndAverageArrayVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
    private String measureUnitName;
}
