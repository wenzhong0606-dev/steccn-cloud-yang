package com.steccn.platform.vo;

import lombok.Data;

/**
 * 安环系统看板
 *
 * @author AndrewLuo
 * @date 2024/07/01
 */
@Data
public class SafetyCockpitVO {
    private String RISK_LEVEL02_COUNT;
    private String DS_DANGEROUS_BETTER1;
    private String RISK_LEVEL03_COUNT;
    private String DS_DANGEROUS_BETTER2;
    private String RISK_LEVEL01_CHECK_RATE;
    private String DS_DANGEROUS_BETTER3;
    private String RISK_LEVEL03_NOCHECK;
    private String DS_DANGEROUS_BETTER4;
    private Integer TR_RISK_LEVEL02;
    private String RISK_LEVEL03_CHECK_RATE;
    private Integer TR_RISK_LEVEL01;
    private String RISK_LEVEL02_NOCHECK;
    private String RISK_LEVEL01_NOCHECK;
    private String RISK_LEVEL04_NOCHECK;
    private String RISK_LEVEL04_COUNT;
    private String RISK_LEVEL01_COUNT;
    private String RISK_LEVEL02_CHECK_RATE;
    private Integer TR_RISK_LEVEL04;
    private Integer TR_RISK_LEVEL03;
    private String RISK_LEVEL04_CHECK_RATE;
}
