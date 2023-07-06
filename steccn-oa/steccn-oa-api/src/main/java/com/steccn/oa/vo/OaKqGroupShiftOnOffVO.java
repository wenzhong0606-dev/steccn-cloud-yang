package com.steccn.oa.vo;

import lombok.Data;

/**
 * Oa班次开始结束时间
 *
 * @author AndrewLuo
 * @date 2023/07/06
 */
@Data
public class OaKqGroupShiftOnOffVO {
    /**
     * 时间
     */
    private String times;

    /**
     * 开始结束
     */
    private String onOffWorkType;

    /**
     * 是否第二天
     */
    private String across;

    /**
     * 记录
     */
    private String record;
}
