package com.steccn.mes.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * erp客户DTO
 *
 * @author AndrewLuo
 * @date 2023/07/26
 */
@Data
public class ErpCustomerDTO implements Serializable {
    /**
     * 开始时间
     */
    private LocalDateTime startfmodifydate;
    /**
     * 结束时间
     */
    private LocalDateTime stopfmodifydate;
    /**
     * 每页显示条数
     */
    private Integer pagesize;
    /**
     * 页码
     */
    private Integer pageindex;
}
