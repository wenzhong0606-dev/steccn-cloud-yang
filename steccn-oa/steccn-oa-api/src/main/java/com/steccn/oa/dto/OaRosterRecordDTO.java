package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * oa排版记录dto
 *
 * @author AndrewLuo
 * @date 2023/03/02
 */
@Data
public class OaRosterRecordDTO implements Serializable {
    /**
     * 开始日期
     */
    private String startdate;
    /**
     * 结束日期
     */
    private String enddate;
    /**
     * 岗位id
     */
    private String jobtitle;
}
