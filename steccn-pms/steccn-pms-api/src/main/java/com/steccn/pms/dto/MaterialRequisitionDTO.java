package com.steccn.pms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * 领料单DTO
 *
 * @author Mongknife
 * @date 2022/11/3
 */
@Data
@ApiModel("领料单DTO")
public class MaterialRequisitionDTO {

    @ApiModelProperty("id")
    @NotNull(groups = UpdateGroup.class, message = "编辑数据时主键不能为空")
    private Long id;

    @ApiModelProperty("类型id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "编辑数据时类型不能为空")
    private Long typeId;

    @ApiModelProperty("部门id")
//    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "编辑数据时部门不能为空")
    private Integer departmentId;

    @ApiModelProperty("仓库id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "编辑数据时仓库不能为空")
    private Long warehouseId;

    @ApiModelProperty("备注")
    @Length(groups = {InsertGroup.class, UpdateGroup.class}, max = 100, message = "备注长度不超过100个中文")
    private String note;

    /**
     * 关联单据
     */
    private String associatedNo;

    @ApiModelProperty("领料单详情")
    @Valid
    private List<MaterialRequisitionInfoDTO> list;
}
