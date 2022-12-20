package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 平均数vo
 *
 * @author AndrewLuo
 * @date 2022/09/07
 */
@Data
public class QtAverageArrayVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
    private String measureUnitName;
}
