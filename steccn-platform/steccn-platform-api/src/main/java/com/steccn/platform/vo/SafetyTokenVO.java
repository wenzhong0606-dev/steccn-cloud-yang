package com.steccn.platform.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 安环tokenVO
 *
 * @author AndrewLuo
 * @date 2023/07/31
 */
@Data
public class SafetyTokenVO implements Serializable {
//    private Integer sid;
//    private Integer version;
//    private String createBy;
//    private String createdByName;
//    private LocalDateTime createdDt;
//    private String updateBy;
//    private String updateByName;
//    private LocalDateTime updatedDt;
//    private String transactionId;
//    private String pin;
//    private String userNameCn;
//    private String userNameEn;
//    private String userNameVn;
//    private String country;
//    private String sex;
//    private String password;
//    private String passwordText;
//    private String email;
//    private String extTel;
//    private String phone1;
//    private String phone2;
//    private String groupId;
//    private String shiftId;
//    private LocalDateTime pwdModifyDate;
//    private String userSecurityRoles;
//    private String shortName;
//    private String branchCompanyId;
//    private String branchCompanyName;
//    private String workshopId;
//    private String userGroupNames;
//    private String userGroupNames;
    private String token;
    @JSONField(jsonDirect=true)
    private String userInfo;
}
