package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class CustomerComplainPageDTO extends BasePageDTO {

    /**
     * 客诉单号
     */
    private String code;

    /**
     * 标签
     */
    private String title;

    /**
     * 投诉客户
     */
    private Integer complainCustomer;

    /**
     * 投诉接收人
     */
    private Integer complainReceiver;

    /**
     * 投诉类型
     */
    private Integer complainType;

    /**
     * 处理人
     */
    private Integer dealMan;


    /**
     * 0-待处理、1-待审批、2-已关闭
     */
    private Integer status;

    /**
     * 处理方式
     */
    private Integer dealWay;


    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;


    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

}
