package com.steccn.mes.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 点检任务计数
 *
 * @author AndrewLuo
 * @date 2023/09/08
 */
@Data
public class MesInsTaskCountDTO implements Serializable {
    /**
     * 数量
     */
    private Long num;
    /**
     * 所属机构
     */
    private String officeCode;
}
