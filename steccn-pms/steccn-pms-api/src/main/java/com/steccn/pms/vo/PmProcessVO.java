package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工序分页
 *
 * @author Mongknife
 * @date 2022/9/5
 */
@ApiModel("工序分页Vo")
@Data
public class PmProcessVO implements Serializable {


    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("类型")
    private Integer type;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("文件")
    private String fileUrl;


    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新人名称")
    private String updateName;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

}
