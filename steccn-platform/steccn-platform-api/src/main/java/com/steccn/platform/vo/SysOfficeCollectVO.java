package com.steccn.platform.vo;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;

@Data
public class SysOfficeCollectVO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 组织架构类型
     */
    private Integer officeType;

    /**
     * 组织架构id
     */
    private Integer officeId;

    /**
     * 进站0 出站1
     */
    private Integer type;

    /**
     * 采集点编码
     */
    private String code;

    /**
     * 采集点名称
     */
    private String name;

    /**
     * 备注
     */
    private String note;

    private static final long serialVersionUID = 1L;
}
