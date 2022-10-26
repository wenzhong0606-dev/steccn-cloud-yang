package com.steccn.platform.dto;

import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.io.Serializable;

/**
 * 根据父级节点和类型查询数据
 *
 * @author AndrewLuo
 * @date 2022/09/14
 */
@Data
public class SysOfficeParentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 父级code
     */
    private String parentCode;

    /**
     * 机构类型
     */
    private String officeType;

    /**
     * 主键id
     */
    private Integer id;
}
