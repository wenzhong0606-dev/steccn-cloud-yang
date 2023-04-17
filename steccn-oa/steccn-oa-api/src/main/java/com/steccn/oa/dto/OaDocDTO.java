package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 新建Oa文档DTO
 *
 * @author AndrewLuo
 * @date 2023/04/12
 */
@Data
public class OaDocDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文档标题
     */
    private String docSubject;

    /**
     * 目录id
     */
    private Integer seccategory;

    /**
     * html文档内容
     */
    private String doccontent;
}
