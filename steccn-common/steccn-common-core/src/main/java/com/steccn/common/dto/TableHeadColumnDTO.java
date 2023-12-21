package com.steccn.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 复杂表头
 * @date 2023/12/21
 */
@Data
public class TableHeadColumnDTO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 属性值
     */
    private String prop;

    /**
     * 显示值
     */
    private String label;

    /**
     * 孩子节点
     */
    private List<TableHeadColumnDTO> children;
}
