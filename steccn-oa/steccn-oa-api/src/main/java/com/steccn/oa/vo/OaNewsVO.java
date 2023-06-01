package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * oa新闻VO
 *
 * @author AndrewLuo
 * @date 2023/06/01
 */
@Data
public class OaNewsVO implements Serializable {
    /**
     * 新闻Id
     */
    private Integer newsId;

    /**
     * 新闻名称
     */
    private String newsName;

    /**
     * 创建时间
     */
    private String createTime;
}
