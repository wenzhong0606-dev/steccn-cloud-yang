package com.steccn.canal.domain;

import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;

@Data
public class JsSysDictType implements Serializable {
    /**
     * 主键
     */
    @Column(name = "id")
    private String id;

    /**
     * 字典名称
     */
    @Column(name = "dict_name")
    private String dictName;

    /**
     * 字典类型
     */
    @Column(name = "dict_type")
    private String dictType;

    /**
     * 是否系统字典
     */
    @Column(name = "is_sys")
    private String isSys;

    /**
     * 状态（0正常 1删除 2停用）
     */
    @Column(name = "status")
    private String status;

}
