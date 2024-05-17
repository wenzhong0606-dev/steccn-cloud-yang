package com.steccn.hikvision.vo;

import lombok.Data;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/05/17
 */
@Data
public class HikCompanyVO {
    private String companyId;
    private String companyName;
    private String modelDataId;
    private String companyTag;
    private String contact;
    private String contactInfo;
    private Boolean isUsed;
}
