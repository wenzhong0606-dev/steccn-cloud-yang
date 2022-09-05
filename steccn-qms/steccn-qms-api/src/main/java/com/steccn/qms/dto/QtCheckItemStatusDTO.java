package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * @desc 检验项停用启用
 */
@Data
@ApiModel("检验项停用启用dto")
public class QtCheckItemStatusDTO implements Serializable {

    private Integer id;

    private Integer status;
}
