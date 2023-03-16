package com.steccn.qms.vo;

import com.steccn.qms.dto.QtCustomerComplainDetailDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
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
    private List<QtCustomerComplainDetailVO> details;

    /**
     * 抄送人
     */
    private Long carbonCopyMan;

    /**
     * 审批人
     */
    @NotNull(message = "审批人不能为空")
    private Long approveMan;


}
