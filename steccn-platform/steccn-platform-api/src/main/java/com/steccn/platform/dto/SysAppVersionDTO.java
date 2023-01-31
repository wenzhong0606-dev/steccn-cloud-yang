package com.steccn.platform.dto;

import com.steccn.common.dto.BasePageDTO;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * app版本控制DTO
 *
 * @author Mongknife
 * @date 2023/1/31
 */
@Data
@ApiModel("app版本控制DTO")
public class SysAppVersionDTO extends BasePageDTO {

    /**
     * 版本
     */
    @ApiModelProperty("版本号")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "版本号不能为空")
    @Size(min = 1, max = 20, message = "版本号长度不能大于 20个字符")
    private String version;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    @Size(min = 0, max = 100, message = "备注长度不能超过 100个字符")
    private String note;

    /**
     * 文件id
     */
    @ApiModelProperty("app文件id")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "app文件id不能为空")
    private Long fileId;

}
