package com.steccn.wms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/12
 */
@Data
@ApiModel("入库凭证DTO")
public class WmsStorageReceiptDTO {

    @ApiModelProperty("主键")
    @NotNull(groups = UpdateGroup.class, message = "编辑数据时主键不能为空")
    private Long id;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("类型")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "类型id不能为空")
    private Long type;

    @ApiModelProperty("仓库Id")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "仓库id不能为空")
    private Integer warehouseId;

    @ApiModelProperty("仓库名称")
    private String warehouseName;

    @ApiModelProperty("备注")
    @Size(min = 0, max = 256, message = "备注长度不能超过 256 个字符")
    private String note;

    @ApiModelProperty("单据来源类型（0：生产）")
    private Integer sourceType;

    @ApiModelProperty("单据来源单号")
    private String sourceCode;

    @ApiModelProperty("明细集合")
    @Valid
    private List<WmsStorageReceiptInfoDTO> list;
}
