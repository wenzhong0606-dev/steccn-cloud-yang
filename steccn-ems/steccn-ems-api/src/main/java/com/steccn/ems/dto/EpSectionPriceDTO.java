package com.steccn.ems.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 峰谷平分布接收参数类
 *
 * @author AndrewLuo
 * @date 2022/12/06
 */
@Data
public class EpSectionPriceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 开始日期
     */
    private LocalDate dateStart;

    /**
     * 结束日期
     */
    private LocalDate dateEnd;

    /**
     * 节点id
     */
    private Integer nodeId;
}
