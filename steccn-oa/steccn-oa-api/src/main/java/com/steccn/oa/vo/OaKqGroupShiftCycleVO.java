package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Oa周期名字
 *
 * @author AndrewLuo
 * @date 2023/07/06
 */
@Data
public class OaKqGroupShiftCycleVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 周期id
     */
    private Integer cycleId;
    /**
     * 周期名字
     */
    private String cycleName;

    /**
     * 周期天数
     */
    private Integer cycleDay;

    /**
     * 周期详情
     */
    private List<OaKqGroupShiftCycleDetail> cycleDetails;
}
