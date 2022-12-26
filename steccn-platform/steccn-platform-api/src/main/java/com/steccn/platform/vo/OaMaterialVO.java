package com.steccn.platform.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * OA查询物料接口VO
 *
 * @author AndrewLuo
 * @date 2022/12/26
 */
@Data
public class OaMaterialVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 物料code
     */
    private String materialCode;

    /**
     * 物料名字
     */
    private String materialName;

    /**
     * 分类名字
     */
    private String categoryName;
}
