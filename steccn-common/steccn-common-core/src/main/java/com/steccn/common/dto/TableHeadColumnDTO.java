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
     * 空参构造函数
     */
    public TableHeadColumnDTO() {

    }

    /**
     * 双参构造函数
     *
     * @param label label
     * @param prop  prop
     */
    public TableHeadColumnDTO(String label, String prop) {
        this.prop = prop;
        this.label = label;
    }

    /**
     * 全参构造函数
     *
     * @param label    label
     * @param prop     prop
     * @param children children
     */
    public TableHeadColumnDTO(String label, String prop, List<TableHeadColumnDTO> children) {
        this.prop = prop;
        this.label = label;
        this.children = children;
    }


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
