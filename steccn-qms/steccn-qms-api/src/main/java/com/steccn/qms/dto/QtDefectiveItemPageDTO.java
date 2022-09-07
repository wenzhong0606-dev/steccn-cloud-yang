package com.steccn.qms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 * @desc 缺陷项分页dto
 */
@Data
@ApiModel("缺陷项分页dto")
public class QtDefectiveItemPageDTO extends BasePageDTO implements Serializable {

    @ApiModelProperty("缺陷项名称")
    private String name;

    @ApiModelProperty("类别id")
    private Integer categoryId;

}
