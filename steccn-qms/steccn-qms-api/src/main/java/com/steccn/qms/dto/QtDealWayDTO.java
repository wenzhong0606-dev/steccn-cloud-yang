package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 处理方式
 * @date 2023/3/15
 */
@ApiModel("处理方式")
@Data
public class QtDealWayDTO implements Serializable {

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("编码")
    @NotNull(message = "编码不能为空")
    private Integer code;

    @ApiModelProperty("类型 0客诉 1不合格品")
    @NotNull(message = "类型不能为空")
    private Integer type;

    @ApiModelProperty("处理方式")
    @Length(max = 20, message = "处理方式名称长度不能超过20个文字")
    @NotBlank(message = "处理方式名称不能为空")
    private String way;

    @ApiModelProperty("备注")
    @Length(max = 100, message = "备注长度不能超过100个文字")
    private String note;

    private static final long serialVersionUID = 1L;
}
