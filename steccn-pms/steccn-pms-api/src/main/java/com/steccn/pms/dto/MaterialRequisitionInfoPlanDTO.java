package com.steccn.pms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 领料单详情DTO
 *
 * @author Mongknife
 * @date 2022/11/2
 */
@ApiModel("领料单详情DTO")
@Data
public class MaterialRequisitionInfoPlanDTO {


    @ApiModelProperty("物料编码")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物料编码不能为空")
    private String materialCode;

    @ApiModelProperty("物料id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物料id不能为空")
    private Long materialId;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("机构id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "机构不能为空")
    private List<Integer> organization;


    @ApiModelProperty("需求数量")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "需求数量不能为空")
    @DecimalMin(groups = {UpdateGroup.class, InsertGroup.class}, value = "0", message = "需求数量应大于0")
    @DecimalMax(groups = {UpdateGroup.class, InsertGroup.class}, value = "99999999.999", message = "需求数量大小应小于99999999.999")
    @Digits(groups = {UpdateGroup.class, InsertGroup.class}, integer = 8, fraction = 3, message = "整数位上限为8位，小数位上限位3位")
    private String quantity;

    @ApiModelProperty("需求时间")
    private LocalDate requireDate;
}
