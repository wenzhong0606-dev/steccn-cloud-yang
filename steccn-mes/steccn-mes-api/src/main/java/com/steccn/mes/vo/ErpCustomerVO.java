package com.steccn.mes.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/08/11
 */
@Data
public class ErpCustomerVO {
    /**
     * 客户id
     */
    private Integer FCUSTID;
    /**
     * 客户code
     */
    private String FNUMBER;
    /**
     * 客户名字
     */
    private String FNAME;
    /**
     * 单据状态
     */
    private String FDOCUMENTSTATUS;
    /**
     * 禁用状态
     */
    private String FFORBIDSTATUS;
    /**
     * 使用组织
     */
    private String ORGNAME;
    /**
     * 审核人
     */
    private String FAPPROVERIDFNAME;
    /**
     * 审核日期
     */
    private LocalDateTime FAPPROVEDATE;
    /**
     *  客户分组
     */
    private String FGROUPFNAMEW;

    /**
     * 注册地址
     */
    private String FREGISTERADDRESS;
    /**
     * 纳税登记号
     */
    private String FTAXREGISTERCODE;
    /**
     * 发票抬头
     */
    private String FINVOICETITLE;
    /**
     * 开户银行
     */
    private String FINVOICEBANKNAME;
    /**
     * 统一社会信用代码
     */
    private String FSOCIALCRECODE;
    /**
     * 银行账号
     */
    private String FINVOICEBANKACCOUNT;
    /**
     * 开票联系电话
     */
    private String FINVOICETEL;
    /**
     * 开票通讯地址
     */
    private String FINVOICEADDRESS;
    /**
     * 联系人
     */
    private List<String> FCONTACTLIST;
    /**
     * 联系人名称
     */
    private String FCONTACTNAME;
}
