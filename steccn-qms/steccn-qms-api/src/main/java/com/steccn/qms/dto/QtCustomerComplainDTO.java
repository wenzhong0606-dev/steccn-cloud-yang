package com.steccn.qms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
@ApiModel
public class QtCustomerComplainDTO {

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
     * 描述url
     */
    private String descUrl;

    /**
     * 投诉客户
     */
    private Integer complainCustomer;

    /**
     * 投诉客户名称
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
     * 投诉接收人名称
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
     * 回复客户日期
     */
    private LocalDate replyClientDate;

    /**
     * 处理人
     */
//    @NotNull(message = "处理人不能为空")
    private Long dealMan;

    /**
     * 处理人名称
     */
    private String dealManName;

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
     * 客诉明细
     */
    @Valid
    private List<QtCustomerComplainDetailDTO> details;

    /**
     * 抄送人
     */
    private Long carbonCopyMan;

    private List<QtCarbonCopyManDTO> manList;

    private List<QtCustomerComplainModuleDTO> modules;

    /**
     * 审批人
     */
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class},message = "审批人不能为空")
    private Long approveMan;


    /**
     * 客诉详情页url
     */
    private String infoUrl;


}
