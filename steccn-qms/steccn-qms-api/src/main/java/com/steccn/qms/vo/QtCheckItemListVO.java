package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @desc 检验项分页vo
 */
@ApiModel("检验项分页vo")
@Data
public class QtCheckItemListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("检验项名称")
    private String name;

    @ApiModelProperty("数据类型")
    private Long dataType;

    @ApiModelProperty("数据类型")
    private String dataTypeName;

    @ApiModelProperty("计量单位")
    private Long measureUnit;

    @ApiModelProperty("计量单位")
    private String measureUnitName;

    @ApiModelProperty("可能值")
    private List<String> possibleValue;

    @ApiModelProperty("默认值")
    private List<String> defaultValue;

    @ApiModelProperty("检验项类别id")
    private Integer categoryId;

    @ApiModelProperty(" 状态：0停用  1启用")
    private Integer status;

    @ApiModelProperty("操作说明")
    private String opDesc;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
