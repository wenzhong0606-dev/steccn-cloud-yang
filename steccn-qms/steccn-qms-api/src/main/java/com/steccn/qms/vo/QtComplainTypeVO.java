package com.steccn.qms.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 投诉类型
 * @date 2023/3/15
 */
@ApiModel("投诉类型")
@Data
public class QtComplainTypeVO implements Serializable {

    @ApiModelProperty("主键")
    private Integer id;

    @ApiModelProperty("投诉类型名称")
    private String type;

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
