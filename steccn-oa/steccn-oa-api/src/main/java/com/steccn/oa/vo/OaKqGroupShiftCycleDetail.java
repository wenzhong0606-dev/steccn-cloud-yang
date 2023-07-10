package com.steccn.oa.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * oa排班周期详情
 *
 * @author AndrewLuo
 * @date 2023/07/06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OaKqGroupShiftCycleDetail implements Serializable {
    /**
     * 第几天
     */
    private String day;
    /**
     * 班次
     */
    private OaKqGroupShiftManageVO shiftManagement;
}
