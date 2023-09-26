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
import java.time.LocalDateTime;
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
    private Integer status;

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

    @ApiModelProperty("创建人")
    private Long createBy;

    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("是否SRM系统传参 0否 1是")
    private Integer isSRMSys;

    @ApiModelProperty("操作人姓名")
    private String operateName;


    @ApiModelProperty("操作人id")
    private Integer operateId;

    @ApiModelProperty("操作时间")
    private LocalDateTime operateTime;

    @ApiModelProperty("金蝶收料通知单编码")
    private String erpReceiptCode;

    @ApiModelProperty("金蝶收料通知单Id")
    private String erpReceiptId;

    @ApiModelProperty("入库时间")
    private LocalDateTime comeTime;
}
