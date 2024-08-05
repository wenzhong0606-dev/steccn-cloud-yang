package com.steccn.sales.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName res_users
 */
@TableName(value ="res_users")
@Data
public class ResUsers implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 
     */
    private Integer companyId;

    /**
     * 
     */
    private Integer partnerId;

    /**
     * 
     */
    private Boolean active;

    /**
     * 
     */
    private LocalDateTime createDate;

    /**
     * 
     */
    private String login;

    /**
     * 
     */
    private String password;

    /**
     * Home Action
     */
    private Integer actionId;

    /**
     * Created by
     */
    private Integer createUid;

    /**
     * Last Updated by
     */
    private Integer writeUid;

    /**
     * Email Signature
     */
    private String signature;

    /**
     * Share User
     */
    private Boolean share;

    /**
     * Last Updated on
     */
    private LocalDateTime writeDate;

    /**
     * Totp Secret
     */
    private String totpSecret;

    /**
     * Notification
     */
    private String notificationType;

    /**
     * OdooBot Status
     */
    private String odoobotState;

    /**
     * Odoobot Failed
     */
    private Boolean odoobotFailed;

    /**
     * User Sales Team
     */
    private Integer saleTeamId;

    /**
     * Won in Opportunities Target
     */
    private Integer targetSalesWon;

    /**
     * Activities Done Target
     */
    private Integer targetSalesDone;

    /**
     * Invoiced in Sales Orders Target
     */
    private Integer targetSalesInvoiced;

    /**
     * OAuth Provider
     */
    private Integer oauthProviderId;

    /**
     * OAuth User ID
     */
    private String oauthUid;

    /**
     * OAuth Access Token
     */
    private String oauthAccessToken;

    /**
     * 物流公司
     */
    private Integer logisticsCompanyId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}