package com.steccn.platform.vo;


import lombok.Data;

import java.io.Serializable;

/**
 * 父亲节点挂子id已经名称
 */
@Data
public class SysOfficeParentVO implements Serializable {
    private Integer id;
    private String  officeName;
}
