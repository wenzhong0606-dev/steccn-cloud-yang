package com.steccn.platform.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 字典DTO
 *
 * @author Mongknife
 * @date 2022/9/14
 */
@Data
@ApiModel("字典DTO")
public class SysDictDTO {

    @ApiModelProperty("编码")
    private List<String> codeList;

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("主键")
    @NotNull(groups = UpdateGroup.class, message = "主键不能为空")
    private Long id;

    /**
     * 名称
     */
    @ApiModelProperty("名称")
    @NotNull(groups ={ UpdateGroup.class, InsertGroup.class}, message = "名称不能为空")
    private String name;

    /**
     * 编码
     */
    @ApiModelProperty("编码")
    @NotNull(groups ={ UpdateGroup.class, InsertGroup.class}, message = "编码不能为空")
    private String code;


    /**
     * 排序
     */
    @ApiModelProperty("排序")
    private Integer sort;


    /**
     * 是否有效 0否1是
     */
    @ApiModelProperty("是否有效 0否1是")
    private Integer useFlag;

    /**
     * 父id
     */
    @ApiModelProperty("父id")
    @NotNull(groups ={ UpdateGroup.class, InsertGroup.class}, message = "父级节点id不能为空")
    private Long parentId;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新人名称
     */
    private String updateName;

    /**
     * 更新人时间
     */
    private LocalDateTime updateTime;


}
