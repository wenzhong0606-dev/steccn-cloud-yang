package com.steccn.mes.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备客户VO
 *
 * @author AndrewLuo
 * @date 2023/07/26
 */
@Data
public class MesCutomerVO implements Serializable {
    private String customerCode;
    private String name;
    private LocalDateTime lastModifiedDateTime;
    /**
     * 单据状态
     */
    private String customerStatus;

    /**
     * 禁用状态
     */
    private String forbidStatus;

    /**
     * 使用组织
     */
    private String orgName;

    /**
     * 审核人
     */
    private String approverName;

    /**
     * 审核日期
     */
    private LocalDateTime approveDate;

    /**
     * 客户分组
     */
    private String groupName;

    /**
     * 注册地址
     */
    private String registerAddress;

    /**
     * 纳税登记号
     */
    private String taxRegisterCode;

    /**
     * 发票抬头
     */
    private String invoiceTitle;

    /**
     * 开户银行
     */
    private String invoiceBankName;

    /**
     * 统一社会信用代码
     */
    private String socialCreCode;

    /**
     * 银行账号
     */
    private String invoiceBankAccount;

    /**
     * 开票联系电话
     */
    private String invoiceTel;

    /**
     * 开票通讯地址
     */
    private String invoiceAddress;

    /**
     * 联系人
     */
    private List<String> contactList;

    /**
     * 联系人名称
     */
    private String contactName;
}
