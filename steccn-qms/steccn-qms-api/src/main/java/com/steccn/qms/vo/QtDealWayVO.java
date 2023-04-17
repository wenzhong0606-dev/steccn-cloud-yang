package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 处理方式
 * @date 2023/3/15
 */
@ApiModel("处理方式")
@Data
public class QtDealWayVO implements Serializable {

    @ApiModelProperty("主键")
    private Integer id;


    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("类型 0客诉 1不合格品")
    private Integer type;

    @ApiModelProperty("处理方式")
    private String way;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("最后修改时间")
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}
