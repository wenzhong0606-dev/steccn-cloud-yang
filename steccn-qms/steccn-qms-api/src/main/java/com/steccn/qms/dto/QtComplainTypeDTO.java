package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author yequan
 * @desc 投诉类型
 * @date 2023/3/15
 */
@ApiModel("投诉类型")
@Data
public class QtComplainTypeDTO implements Serializable {

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("投诉类型名称")
    @Length(max = 20, message = "投诉类型名称长度不能超过20个文字")
    @NotBlank(message = "投诉类型名称不能为空")
    private String type;

    @ApiModelProperty("备注")
    private String note;

    private static final long serialVersionUID = 1L;
}
