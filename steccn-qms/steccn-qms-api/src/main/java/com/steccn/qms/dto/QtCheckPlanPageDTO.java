package com.steccn.qms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 检验方案查询dto
 *
 * @author AndrewLuo
 * @date 2022/09/07
 */
@Data
public class QtCheckPlanPageDTO extends BasePageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("检验方案名称")
    private String name;
}
