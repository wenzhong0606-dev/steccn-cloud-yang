package com.setccn.platfrom.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/6
 */
@Data
@ApiModel("机构数据dto")
public class SysOfficeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("机构名称")
    private String officeName;

    @ApiModelProperty("机构编码")
    private String officeCode;

    @ApiModelProperty("机构类型")
    private String officeType;
}
