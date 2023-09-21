package com.steccn.platform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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
    private Integer sid;
    private Integer version;
    private String createBy;
    private String createdByName;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDt;
    private String updateBy;
    private String updateByName;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDt;
    private String transactionId;
    private String pin;
    private String userNameCn;
    private String userNameEn;
    private String userNameVn;
    private String country;
    private String sex;
    private String password;
    private String passwordText;
    private String email;
    private String extTel;
    private String phone1;
    private String phone2;
    private String groupId;
    private String shiftId;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pwdModifyDate;
    private String userSecurityRoles;
    private String shortName;
    private String branchCompanyId;
    private String branchCompanyName;
    private String workshopId;
    private String userGroupNames;
    private String roleNames;
    private Integer staffSid;
    private Integer departmentSid;
    private String departmentName;
    private String themeType;
    private String company;
    private String sourcePassword;
    private String token;
    private String pinyinall;
    private String controlFlag;
    private String openid;
    private String appletsOpenId;
    private String wxState;
    private String superAdmin;
    private String pinyinshort;
    private String userState;
    private String submitByFace;
    private String identityId;
    private String jobNumber;
    private String orgFullName;
    private Integer postSid;
    private String postId;
    private String postName;
    private String orgType;
    private String mainpageType;
    private String deptCode;
    private String deptName;
    private String authSid;
    private String authOrgSid;
    private String authPostSid;
    private String userLabelType;
    private String userLabelTypeName;
    private SafetyTokenPageMapVO mainPageMap;
}
