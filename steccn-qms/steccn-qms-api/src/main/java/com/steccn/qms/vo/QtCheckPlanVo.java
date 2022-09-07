package com.steccn.qms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 检验方案分页vo
 * @author AndrewLuo
 * @date 2022/09/07
 */
@Data
public class QtCheckPlanVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("检验方案名称")
    private String name;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty(" 状态：0停用  1启用")
    private Integer status;

    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
