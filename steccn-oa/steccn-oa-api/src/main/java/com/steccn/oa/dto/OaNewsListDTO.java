package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * oa新闻列表DTO
 *
 * @author AndrewLuo
 * @date 2023/06/01
 */
@Data
public class OaNewsListDTO implements Serializable {
    /**
     * 新闻id
     */
    private Integer id;

    /**
     * 新闻页名称
     */
    private String frontpagename;

    /**
     * 是否活跃
     */
    private String isactivespan;

    /**
     * 内部外部
     */
    private String publishtypespan;

}
