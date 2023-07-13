package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Oa班次VO
 *
 * @author AndrewLuo
 * @date 2023/07/06
 */
@Data
public class OaKqGroupShiftManageVO implements Serializable {
    /**
     * 班次id
     */
    private Integer id;
    /**
     * 班次名字
     */
    private String name;
    /**
     * 具体时间
     */
    private List<OaKqGroupShiftOnOffVO> timeDetail;

    /**
     * 时间范围
     */
    private OaKqGroupTimeRangeVO timeRange;
}
