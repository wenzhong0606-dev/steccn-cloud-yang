package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询所有已启用的物料VO
 *
 * @author AndrewLuo
 * @date 2022/09/14
 */
@Data
public class QtMaterialListAllVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 物料id
     */
    private Long id;

    /**
     * 物料名字
     */
    private String name;

    /**
     *
     */
    private String code;
}
