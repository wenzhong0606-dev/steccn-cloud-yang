package com.steccn.platform.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

/**
 * 备件分页DTO
 *
 * @author Mongknife
 * @date 2022/11/7
 */

@Data
@ApiModel("备件分页DTO")
public class SparePageDTO extends BasePageDTO {

    @ApiModelProperty("物料名称")
    private String name;
}
