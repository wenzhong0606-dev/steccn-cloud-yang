package com.steccn.wms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 盘点区域
 */
@Data
public class WmsCheckAreaVO implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("名称")
    private String name;

    private static final long serialVersionUID = 1L;
}