package com.steccn.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 前端vo
 */
@Data
public class FrontTableElement implements Serializable {

    /**
     * 显示
     */
    private String label;

    /**
     * 属性
     */
    private String prop;

    /**
     * 孩子节点
     */
    private List<FrontTableElement> children;

    public FrontTableElement(String label) {
        this.label = label;
    }

    public FrontTableElement(String label, String prop) {
        this.label = label;
        this.prop = prop;
    }

    public FrontTableElement(String label, String prop, List<FrontTableElement> children) {
        this.label = label;
        this.prop = prop;
        this.children = children;
    }
}
