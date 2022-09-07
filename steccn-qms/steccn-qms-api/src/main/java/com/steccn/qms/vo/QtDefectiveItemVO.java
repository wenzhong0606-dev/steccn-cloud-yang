package com.steccn.qms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @desc 缺陷项vo
 */
@Data
@ApiModel("缺陷项vo")
public class QtDefectiveItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键id")
    private Integer id;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("缺陷项名称")
    private String name;

    @ApiModelProperty("缺陷项类别id")
    private Integer categoryId;

    @ApiModelProperty("状态：0停用  1启用")
    private Integer status;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("更新人时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
