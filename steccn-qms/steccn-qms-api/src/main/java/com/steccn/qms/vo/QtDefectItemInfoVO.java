package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2023/1/10
 */
@Data
@ApiModel("缺陷项vo")
public class QtDefectItemInfoVO {

    @ApiModelProperty("缺陷项id")
    private Integer id;

    @ApiModelProperty("缺陷项名称")
    private String name;

    @ApiModelProperty("班次id")
    private Integer classesId;

    @ApiModelProperty("班次名称")
    private String  className;

    @ApiModelProperty("缺陷项类别id")
    private Integer categoryId;

    @ApiModelProperty("缺陷项类别名称")
    private String categoryName;

    @ApiModelProperty("任务id")
    private Long taskId;

}
