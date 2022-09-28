package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 检验类型数量统计
 *
 * @author AndrewLuo
 * @date 2022/09/13
 */
@Data
public class QtCheckTaskTypeCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 来料检1/首检2/在线检3/巡检4/出货检5
     */
    private Integer typeCode;

    /**
     * 数量
     */
    private Long count;
}
