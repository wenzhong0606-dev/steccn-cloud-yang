package com.steccn.platform.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * mes新增用户DTO
 *
 * @author AndrewLuo
 * @date 2023/05/30
 */
@Data
public class MesUserDTO implements Serializable {
    private String loginCode;
    private String userName;
    private Boolean isNewRecord;
    private String companyCode;
    private String companyName;
    private String officeCode;
    private String officeName;
}
