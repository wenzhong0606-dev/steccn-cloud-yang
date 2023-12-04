package com.steccn.sales.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName res_partner
 */
@TableName(value ="res_partner")
@Data
public class ResPartner implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 
     */
    private Object companyId;

    /**
     * 
     */
    private Date createDate;

    /**
     * 
     */
    private String name;

    /**
     * Title
     */
    private Object title;

    /**
     * Related Company
     */
    private Object parentId;

    /**
     * Salesperson
     */
    private Object userId;

    /**
     * State
     */
    private Object stateId;

    /**
     * Country
     */
    private Object countryId;

    /**
     * Industry
     */
    private Object industryId;

    /**
     * Color Index
     */
    private Object color;

    /**
     * Commercial Entity
     */
    private Object commercialPartnerId;

    /**
     * Created by
     */
    private Object createUid;

    /**
     * Last Updated by
     */
    private Object writeUid;

    /**
     * Display Name
     */
    private String displayName;

    /**
     * Reference
     */
    private String ref;

    /**
     * Language
     */
    private String lang;

    /**
     * Timezone
     */
    private String tz;

    /**
     * Tax ID
     */
    private String vat;

    /**
     * Company ID
     */
    private String companyRegistry;

    /**
     * Website Link
     */
    private String website;

    /**
     * Job Position
     */
    private String function;

    /**
     * Address Type
     */
    private String type;

    /**
     * Street
     */
    private String street;

    /**
     * Street2
     */
    private String street2;

    /**
     * Zip
     */
    private String zip;

    /**
     * City
     */
    private String city;

    /**
     * Email
     */
    private String email;

    /**
     * Phone
     */
    private String phone;

    /**
     * Mobile
     */
    private String mobile;

    /**
     * Company Name Entity
     */
    private String commercialCompanyName;

    /**
     * Company Name
     */
    private String companyName;

    /**
     * Date
     */
    private Date date;

    /**
     * Notes
     */
    private String comment;

    /**
     * Geo Latitude
     */
    private Integer partnerLatitude;

    /**
     * Geo Longitude
     */
    private Integer partnerLongitude;

    /**
     * Active
     */
    private Boolean active;

    /**
     * Employee
     */
    private Boolean employee;

    /**
     * Is a Company
     */
    private Boolean isCompany;

    /**
     * Share Partner
     */
    private Boolean partnerShare;

    /**
     * Last Updated on
     */
    private Date writeDate;

    /**
     * Main Attachment
     */
    private Object messageMainAttachmentId;

    /**
     * Bounce
     */
    private Object messageBounce;

    /**
     * Normalized Email
     */
    private String emailNormalized;

    /**
     * Signup Token
     */
    private String signupToken;

    /**
     * Signup Token Type
     */
    private String signupType;

    /**
     * Signup Expiration
     */
    private Date signupExpiration;

    /**
     * Last notification marked as read from base Calendar
     */
    private Date calendarLastNotifAck;

    /**
     * Sales Team
     */
    private Object teamId;

    /**
     * Company database ID
     */
    private Object partnerGid;

    /**
     * Additional info
     */
    private String additionalInfo;

    /**
     * Sanitized Number
     */
    private String phoneSanitized;

    /**
     * Supplier Rank
     */
    private Object supplierRank;

    /**
     * Customer Rank
     */
    private Object customerRank;

    /**
     * Invoice
     */
    private String invoiceWarn;

    /**
     * Message for Invoice
     */
    private String invoiceWarnMsg;

    /**
     * Payable Limit
     */
    private Integer debitLimit;

    /**
     * Latest Invoices & Payments Matching Date
     */
    private Date lastTimeEntriesChecked;

    /**
     * Sales Warnings
     */
    private String saleWarn;

    /**
     * Message for Sales Order
     */
    private String saleWarnMsg;

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 客户类别
     */
    private String customerCategory;

    /**
     * 客户分组
     */
    private String customerGroups;

    /**
     * 客户状态
     */
    private String customerStatus;

    /**
     * 是否禁用
     */
    private String isDisable;

    /**
     * 使用组织
     */
    private String orgName;

    /**
     * 审核人
     */
    private String approveName;

    /**
     * 发票抬头
     */
    private String invoiceTitle;

    /**
     * 开户银行
     */
    private String bankName;

    /**
     * 统一社会信用代码
     */
    private String creditCode;

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 开票联系电话
     */
    private String invoiceTel;

    /**
     * 开票通讯地址
     */
    private String invoiceAdds;

    /**
     * 联系人
     */
    private String contactList;

    /**
     * 联系人名称
     */
    private String contactName;

    /**
     * 审核日期
     */
    private Date approveDate;

    /**
     * 里程(KM)
     */
    private Double mileage;

    /**
     * 价格(元/吨)
     */
    private Double price;

    /**
     * 吨公里运费(元/吨/公里)
     */
    private Integer tonPrice;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}