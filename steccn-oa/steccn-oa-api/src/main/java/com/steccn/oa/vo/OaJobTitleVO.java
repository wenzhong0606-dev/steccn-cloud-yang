package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Oa岗位信息VO
 *
 * @author AndrewLuo
 * @date 2023/05/30
 */
@Data
public class OaJobTitleVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 岗位id
     */
    private Integer id;

    /**
     * 岗位名字
     */
    private String jobTitleName;

    /**
     * 岗位Code
     */
    private String jobTitleCode;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}
