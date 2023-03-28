package com.steccn.oa.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * OA工作流操作人DTO
 *
 * @author AndrewLuo
 * @date 2023/03/28
 */
@Data
public class OaWorkFlowOperatorDTO extends OaUserIdAndName{
    /**
     * 操作日期
     */
    private LocalDate operateDate;
    /**
     * 操作时间
     */
    private LocalTime operateTime;

}
