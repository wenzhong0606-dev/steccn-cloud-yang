package com.steccn.qms.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtCarbonCopyManDTO;
import com.steccn.qms.dto.QtCustomerComplainDetailDTO;
import com.steccn.qms.dto.QtCustomerComplainModuleDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel
public class QtCustomerComplainVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 客诉单号
     */
    private String code;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 投诉客户
     */
    private Integer complainCustomer;

    /**
     * 投诉客户
     */
    private String complainCustomerName;

    /**
     * 投诉日期
     */
    private LocalDate complainDate;

    /**
     * 投诉接收人
     */
    private Long complainReceiver;


    /**
     * 投诉接收人
     */
    private String complainReceiverName;

    /**
     * 投诉类型
     */
//    @NotNull(message = "投诉类型不能为空")
    private Integer complainType;


    /**
     * 投诉类型名称
     */
    private String complainTypeName;

    /**
     * 处理人
     */
//    @NotNull(message = "处理人不能为空")
    private Long dealMan;


    /**
     * 处理人名称
     */
    private String  dealManName;

    /**
     * 处理方式
     */
//    @NotNull(message = "处理方式不能为空")
    private Integer dealWay;

    /**
     * 处理方式名称
     */
    private String dealWayName;

    /**
     * 0-待处理、1-待审批、2-已关闭
     */
    private Integer status;

    /**
     * 备注
     */
    private String note;

    /**
     * 回复客户日期
     */
    private LocalDate replyClientDate;

    /**
     * 客诉明细
     */
    @Valid
    private List<QtCustomerComplainDetailVO> details;

    private List<QtCustomerComplainModelVO> modules;

    /**
     * 抄送人
     */
    private List<QtCarbonCopyManDTO> manList;


    /**
     * 抄送人名称
     */
    private String carbonCopyManName;

    /**
     * 审批人
     */
    @NotNull(message = "审批人不能为空")
    private Long approveMan;


    /**
     * 审批人名称
     */
    private String approveManName;


    /**
     * 创建人名称
     */
    private String createName;


    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


    /**
     * 客诉进度
     */
    private List<QtComplainDefectProgressVO> progressList;


    /**
     * 关联整改
     */
    private List<QtImproveRecordVO> improveRecordList;



    /**
     * 中断按钮是否可见
     */
    private Integer suspendFlag = QtManagerConstant.NUM_0;


    /**
     * 退回按钮是否可见
     */
    private Integer sendBackFlag = QtManagerConstant.NUM_0;


    /**
     * 审批按钮是否可见
     */
    private Integer approveFlag = QtManagerConstant.NUM_0;


    /**
     * 提交按钮是否可见
     */
    private Integer submitFlag = QtManagerConstant.NUM_0;


    /**
     * 处理按钮是否可见
     */
    private Integer disposeFlag = QtManagerConstant.NUM_0;

    /**
     * 编辑按钮是否可见
     */
    private Integer editFlag = QtManagerConstant.NUM_0;



}
