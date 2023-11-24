package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


@Data
@ApiModel("分类")
public class ClassificationVO implements Serializable {
    /**
     * 主键
     */
    private String id;

    /**
     * 分类编码
     */
    private String classificationCode;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父级编号
     */
    private String parentCode;

    /**
     * 层级
     */
    private BigDecimal treeLevel;

    /**
     * 是否最末级
     */
    private String treeLeaf;


    /**
     * 子级节点
     */
    private List<ClassificationVO> children;

}
