package com.steccn.canal.domain;

import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;

@Data
public class JsSysDictData implements Serializable {
    @Column(name= "dict_code")
    private String dictCode;
    @Column(name = "parent_code")
    private String parentCode;
    @Column(name = "parent_codes")
    private String parentCodes;

    @Column(name = "dict_type")
    private String dictType;

    @Column(name = "dict_label")
    private String dictLabel;

    @Column(name ="dict_value")
    private String dictValue;

    @Column(name = "tree_sort")
    private String treeSort;

    /**
     * 状态（0正常 1删除 2停用）
     */
    @Column(name = "status")
    private String status;
}
