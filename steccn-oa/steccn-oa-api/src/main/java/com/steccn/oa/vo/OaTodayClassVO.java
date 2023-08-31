package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * oa当班信息VO
 *
 * @author AndrewLuo
 * @date 2023/08/31
 */
@Data
public class OaTodayClassVO implements Serializable {
    /**
     * 班次
     */
    private String classOrder;
    /**
     * 班组
     */
    private String classGroup;
    /**
     * 班长
     */
    private String classLeader;
}
