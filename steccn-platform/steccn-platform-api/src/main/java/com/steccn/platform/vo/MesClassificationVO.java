package com.steccn.platform.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * mes分类
 *
 * @author AndrewLuo
 * @date 2022/12/26
 */
@Data
public class MesClassificationVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分类名字
     */
    private String name;

    /**
     * 分类id
     */
    private String id;

    /**
     * 分类code
     */
    private String classificationCode;
}
