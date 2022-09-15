package com.steccn.pms.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.pms.vo.PmMaterialVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料清单DTO
 *
 * @author Mongknife
 * @date 2022/9/6
 */
@Data
@ApiModel("物料清单dto")
public class PmBillOfMaterialDTO extends BasePageDTO implements Serializable {

    private static final long serialVersionUID = 1L;


    @NotNull(groups = UpdateGroup.class, message = "编辑数据时主键不能为空")
    @ApiModelProperty("主键")
    private Long id;

    @NotNull(groups = UpdateGroup.class, message = "编码不能为空")
    @ApiModelProperty("清单code")
    private String code;
    @NotNull(groups ={ UpdateGroup.class, InsertGroup.class}, message = "编码不能为空")
    @ApiModelProperty("物料名称")
    private String materialName;

    @NotNull(groups ={ UpdateGroup.class, InsertGroup.class}, message = "物料id不能为空")
    @ApiModelProperty("物料id")
    private Long materialId;

    @NotNull(groups ={ UpdateGroup.class, InsertGroup.class}, message = "物料编码不能为空")
    @ApiModelProperty("物料编码")
    private String materialCode;

    @NotNull(groups ={ UpdateGroup.class, InsertGroup.class}, message = "状态不能为空")
    @ApiModelProperty("状态： 1有效 2无效")
    private Integer status;

    @NotNull(groups ={ UpdateGroup.class, InsertGroup.class}, message = "标准数量不能为空")
    @ApiModelProperty("标准数量")
    private Integer quantity;

    @ApiModelProperty("单位id")
    @NotNull(groups ={ UpdateGroup.class, InsertGroup.class}, message = "单位不能为空")
    private Long unitId;

    @ApiModelProperty("版本")
    @NotNull(groups ={ UpdateGroup.class, InsertGroup.class}, message = "版本不能为空")
    private String version;

    @NotNull(groups ={ UpdateGroup.class, InsertGroup.class}, message = "版本标识不能为空")
    @ApiModelProperty("是否当前版本 1是0否")
    private Integer isCurrentVersion;

    @ApiModelProperty("有效开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validBeginTime;

    @ApiModelProperty("有效结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validEndTime;

    @ApiModelProperty("备注")
    @Length(groups = {InsertGroup.class, UpdateGroup.class}, max = 100, message = "备注长度不超过100个中文")
    private String note;

    @ApiModelProperty("物料组件")
    private List<PmMaterialDTO> list;
}
