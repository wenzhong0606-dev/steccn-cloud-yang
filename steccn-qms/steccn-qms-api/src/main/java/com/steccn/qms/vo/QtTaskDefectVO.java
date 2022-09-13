package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 任务-分类-缺陷项
 *
 * @author AndrewLuo
 * @date 2022/09/13
 */
@Data
public class QtTaskDefectVO implements Serializable {
    /**
     * 缺陷项id
     */
    private Integer id;

    /**
     * 缺陷项名字
     */
    private String name;

}
