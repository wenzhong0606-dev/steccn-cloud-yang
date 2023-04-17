package com.steccn.qms.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 不合格品记录分页
 */
@Data
public class QtDefectProductPageDTO  extends BasePageDTO {


    /**
     * 不合格品处理单号
     */
    private String code;

    /**
     * 标题
     */
    private String title;

    /**
     * 检验任务单号
     */
    private String taskCode;

    /**
     * 检验任务类型
     */
    private Long taskType;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 处理人
     */
    private Long dealMan;

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
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束日期
     */
    private LocalDate endDate;


    /**
     * 结束时间
     */
    private LocalDateTime endTime;


    /**
     * 检验完成人员
     */
    private Long taskFinishMan;

}
