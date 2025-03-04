package com.steccn.oa.vo;

import lombok.Data;

/**
 * 新闻公告VO
 *
 * @author AndrewLuo
 * @date 2025/02/20
 */
@Data
public class OaHpElementNoticeVO {
    private Integer id;

    private String title;

    /**
     *
     */
    private String content;

    /**
     *
     */
    private String imgsrc;
}
