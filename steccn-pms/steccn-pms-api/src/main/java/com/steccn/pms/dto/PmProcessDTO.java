package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/5
 */
@Data
@ApiModel("工序管理保存请求dto")
public class PmProcessDTO extends BasePageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键，新增时为空，编辑时存在")
    @NotNull(groups = UpdateGroup.class, message = "编辑数据时主键不能为空")
    private Long id;

    @ApiModelProperty("工序名称")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "工序名称不能为空")
    private String name;

    @ApiModelProperty("工序编码")
    @NotNull(groups = {UpdateGroup.class}, message = "编辑数据时工序编码不能为空")
    private String code;

    @ApiModelProperty("工序类型")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "工序类型不能为空")
    private Integer type;

    @ApiModelProperty("工序状态")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "工序状态不能为空")
    @Max(value = 1, message = "状态只能是 0停用  1启用")
    @Min(value = 0, message = "状态只能是 0停用  1启用")
    private Integer status;

    @ApiModelProperty("备注")
    @Length(groups = {InsertGroup.class, UpdateGroup.class}, max = 100, message = "备注长度不超过100个中文")
    private String note;

    @ApiModelProperty("文件id")
    private Long fileId;

    @ApiModelProperty("执行工位列表")
    private List<PmProcessStationDTO> list;


    @ApiModelProperty("是否报工")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "是否报工不能为空")
    private Integer isReportingWorks;


    @ApiModelProperty("是否报检")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "是否报检不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer isCustomsInspection;


}
